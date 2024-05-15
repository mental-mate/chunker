package tech.harmonysoft.oss.mentalmate.storage.service.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.stereotype.Component
import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.common.execution.ExecutionContextManager
import tech.harmonysoft.oss.common.execution.withContext
import tech.harmonysoft.oss.mentalmate.context.ContextKey
import tech.harmonysoft.oss.mentalmate.llm.Llm
import tech.harmonysoft.oss.mentalmate.storage.config.LlmConfigProvider
import tech.harmonysoft.oss.mentalmate.storage.config.StorageConfigProvider
import tech.harmonysoft.oss.mentalmate.storage.data.DataStorage
import tech.harmonysoft.oss.mentalmate.storage.data.DataStorageDir
import tech.harmonysoft.oss.mentalmate.storage.data.DataStorageFile
import tech.harmonysoft.oss.mentalmate.storage.meta.MetaStorage
import tech.harmonysoft.oss.mentalmate.storage.model.Stage
import tech.harmonysoft.oss.mentalmate.storage.service.StorageService
import tech.harmonysoft.oss.mentalmate.util.checksum.ChecksumUtil
import tech.harmonysoft.oss.mentalmate.util.time.TimeHelper

@Component
class StorageServiceImpl(
    private val storageConfig: StorageConfigProvider,
    private val llmConfig: LlmConfigProvider,
    private val dataStorage: DataStorage<*>,
    private val metaStorage: MetaStorage,
    private val llm: Llm,
    private val timeHelper: TimeHelper,
    private val executionContextManager: ExecutionContextManager,
    private val logger: Logger
) : StorageService {

    override fun chunkIfNecessary(): ProcessingResult<Collection<DataStorageFile>, String> {
        val inputFiles = dataStorage.listFiles(storageConfig.data.inputDir)
        if (inputFiles.isEmpty()) {
            logger.info("no input files to chunk are found")
            return ProcessingResult.success(emptySet())
        }

        logger.info("found {} input files to chunk candidates", inputFiles.size)
        val partitioned = partition(inputFiles)
        logger.info(
            "found {} files to chunk:\n  *) {}\n{} chunked files: %n{} %n{} stuck files: %n{}",
            partitioned.toProcess.size, partitioned.toProcess.joinToString("\n  *) ") { it.file.name },
            partitioned.processed.size, partitioned.processed.joinToString("\n  *) ") { it.file.name },
            partitioned.stuck.size, partitioned.stuck.joinToString("\n  *) ") { it.file.name },
        )
        return chunk(partitioned.toProcess).also {
            logger.info("finished chunking {} files", partitioned.toProcess.size)
        }
    }

    private fun partition(inputFiles: Collection<DataStorageFile>): PartitionedFiles {
        val withChecksum = calculateChecksums(inputFiles)
        val withStatus = runBlocking {
            withChecksum.map { file ->
                async(Dispatchers.IO) {
                    val status = metaStorage.getValue(getFileKey(file))
                    file to status
                }
            }.awaitAll()
        }
        val (toProcess, processed) = withStatus.partition { (_, status) -> status == null }
        return PartitionedFiles(
            processed = processed.mapNotNull { (file, status) ->
                file.takeIf {
                    status == Stage.DONE.name
                }
            },
            toProcess = toProcess.map { it.first },
            stuck = processed.mapNotNull { (file, status) ->
                file.takeIf {
                    status != Stage.DONE.name
                }
            }
        )
    }

    private fun calculateChecksums(
        inputFiles: Collection<DataStorageFile>
    ): Collection<FileWithChecksum> {
        return runBlocking {
            inputFiles.map { file ->
                async(Dispatchers.IO) {
                    FileWithChecksum(file, ChecksumUtil.calculateChecksum(file.getContent()))
                }
            }.awaitAll()
        }
    }

    private fun chunk(files: Collection<FileWithChecksum>): ProcessingResult<Collection<DataStorageFile>, String> {
        if (files.isEmpty()) {
            logger.info("no files to chunk")
            return ProcessingResult.success(emptySet())
        }
        val statuses = runBlocking {
            val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            files.map { file ->
                coroutineScope.async {
                    executionContextManager.withContext(ContextKey.FILE, file.file.name) {
                        try {
                            file to chunk(file)
                        } catch (e: Exception) {
                            logger.error("got an exception on attempt to chunk file {}", file.file.name, e)
                            file to ProcessingResult.failure("unexpected exception ${e.javaClass.name}: ${e.message}")
                        }
                    }
                }
            }.awaitAll()
        }

        val errors = statuses.mapNotNull {
            if (it.second.success) {
                null
            } else {
                it.second.failureValue
            }
        }
        return if (errors.isEmpty()) {
            ProcessingResult.success(statuses.map { it.first.file })
        } else {
            ProcessingResult.failure("${errors.size} errors: ${errors.joinToString()}")
        }
    }

    private fun getFileKey(file: FileWithChecksum): String {
        return "${file.file.name}-${file.checksum}-chunk"
    }

    private suspend fun chunk(file: FileWithChecksum): ProcessingResult<Unit, String> {
        val result = metaStorage.storeIfNotSet(getFileKey(file), Stage.IN_PROGRESS)
        if (!result.success) {
            val error = "can't chunk file ${file.file.name} with checksum ${file.checksum}: failed " +
                        "to mark the processing as ${Stage.IN_PROGRESS} for it - expected that no mark is " +
                        "set for it, but it has ${result.failureValue}"
            logger.info(error)
            return ProcessingResult.failure(error)
        }

        val response = llm.ask(llmConfig.data.prompt, mapOf("text" to file.file.getContent().reader().readText()))
        val chunks = response.split(llmConfig.data.responseChunkSeparator).map { it.trim() }.filter { it.isNotBlank() }
        runBlocking {
            val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val outputDir = getOutputDir(file)
            chunks.mapIndexed { index, chunk ->
                coroutineScope.async {
                    dataStorage.createFile(outputDir, "chunk-$index", chunk.toByteArray())
                }
            }
        }
        val r = metaStorage.compareAndSet(getFileKey(file), Stage.IN_PROGRESS, Stage.DONE)
        return if (r.success) {
            ProcessingResult.success()
        } else {
            val error = "can not mark file ${file.file.name} with checksum ${file.checksum} as chunked - " +
                        "expected its processing stage to be ${Stage.IN_PROGRESS} but it's ${r.failureValue}"
            logger.error(error)
            ProcessingResult.failure(error)
        }
    }

    private fun getOutputDir(file: FileWithChecksum): DataStorageDir {
        val suffix = timeHelper.getTimestamp()
        return dataStorage.getDir("/chunk/${file.file.name}/${file.checksum}-$suffix")
    }

    private data class PartitionedFiles(
        val processed: Collection<FileWithChecksum>,
        val stuck: Collection<FileWithChecksum>,
        val toProcess: Collection<FileWithChecksum>
    )

    private data class FileWithChecksum(
        val file: DataStorageFile,
        val checksum: String
    )
}