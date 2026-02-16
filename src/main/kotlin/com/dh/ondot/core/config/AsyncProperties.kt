package com.dh.ondot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "async")
data class AsyncProperties(
    val event: EventConfig = EventConfig(),
    val discord: DiscordConfig = DiscordConfig(),
) {

    data class EventConfig(
        val corePoolSize: Int = 4,
        val maxPoolSize: Int = 8,
        val queueCapacity: Int = 500,
    )

    data class DiscordConfig(
        val corePoolSize: Int = 2,
        val maxPoolSize: Int = 4,
        val queueCapacity: Int = 100,
    )
}
