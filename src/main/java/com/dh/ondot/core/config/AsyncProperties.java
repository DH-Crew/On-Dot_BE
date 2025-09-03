package com.dh.ondot.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "async")
public class AsyncProperties {
    
    private final EventConfig event = new EventConfig();
    private final DiscordConfig discord = new DiscordConfig();

    @Data
    public static class EventConfig {
        private int corePoolSize = 4;
        private int maxPoolSize = 8;
        private int queueCapacity = 500;
    }

    @Data
    public static class DiscordConfig {
        private int corePoolSize = 2;
        private int maxPoolSize = 4;
        private int queueCapacity = 100;
    }
}
