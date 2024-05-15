package tech.harmonysoft.oss.mentalmate.storage.config.impl

import org.springframework.stereotype.Component
import tech.harmonysoft.oss.configurario.client.DelegatingConfigProvider
import tech.harmonysoft.oss.configurario.client.factory.ConfigProviderFactory
import tech.harmonysoft.oss.mentalmate.storage.data.DataStorage
import tech.harmonysoft.oss.mentalmate.storage.config.StorageConfig
import tech.harmonysoft.oss.mentalmate.storage.config.StorageConfigProvider

data class RawStorageConfig(
    val inputDir: String
)

@Component
class StorageConfigProviderImpl(
    factory: ConfigProviderFactory,
    storage: DataStorage<*>
) : DelegatingConfigProvider<StorageConfig>(
    factory.build(RawStorageConfig::class.java, "storage.raw") { raw ->
        StorageConfig(
            inputDir = storage.getDir(raw.inputDir)
        )
    }
), StorageConfigProvider