package tech.harmonysoft.oss.mentalmate.storage.config.impl

import org.springframework.stereotype.Component
import tech.harmonysoft.oss.configurario.client.DelegatingConfigProvider
import tech.harmonysoft.oss.configurario.client.factory.ConfigProviderFactory
import tech.harmonysoft.oss.mentalmate.storage.config.LlmConfig
import tech.harmonysoft.oss.mentalmate.storage.config.LlmConfigProvider

data class RawLlmConfig(
    val prompt: String,
    val responseChunkSeparator: String
)

@Component
class LlmConfigProviderImpl(
    factory: ConfigProviderFactory
) : DelegatingConfigProvider<LlmConfig>(
    factory.build(RawLlmConfig::class.java, "llm") { raw ->
        if (raw.responseChunkSeparator.isBlank()) {
            throw IllegalArgumentException("LLM response chunk separator must not be blank")
        }
        LlmConfig(
            prompt = raw.prompt,
            responseChunkSeparator = raw.responseChunkSeparator
        )
    }
), LlmConfigProvider