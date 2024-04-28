package tech.harmonysoft.oss.mentalmate.storage.config

import tech.harmonysoft.oss.configurario.client.ConfigProvider

data class LlmConfig(
    val prompt: String,
    val responseChunkSeparator: String
)

interface LlmConfigProvider : ConfigProvider<LlmConfig>