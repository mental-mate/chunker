package tech.harmonysoft.oss.mentalmate.storage.config

import tech.harmonysoft.oss.configurario.client.ConfigProvider
import tech.harmonysoft.oss.mentalmate.storage.data.DataStorageDir

data class StorageConfig(
    val inputDir: DataStorageDir
)

interface StorageConfigProvider : ConfigProvider<StorageConfig>