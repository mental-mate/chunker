package tech.harmonysoft.oss.mentalmate.storage.service

import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.mentalmate.storage.data.DataStorageFile

interface StorageService {

    fun chunkIfNecessary(): ProcessingResult<Collection<DataStorageFile>, String>
}