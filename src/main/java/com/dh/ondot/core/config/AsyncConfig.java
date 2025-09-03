package com.dh.ondot.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.dh.ondot.core.config.AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR;
import static com.dh.ondot.core.config.AsyncConstants.DISCORD_ASYNC_TASK_EXECUTOR;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {
    
    private final AsyncProperties asyncProperties;

    @Bean(name = EVENT_ASYNC_TASK_EXECUTOR)
    public Executor eventAsyncExecutor() {
        AsyncProperties.EventConfig config = asyncProperties.getEvent();
        return createThreadPoolTaskExecutor(
            config.getCorePoolSize(),
            config.getMaxPoolSize(),
            config.getQueueCapacity(),
            "event-"
        );
    }

    @Bean(name = DISCORD_ASYNC_TASK_EXECUTOR)
    public Executor discordAsyncExecutor() {
        AsyncProperties.DiscordConfig config = asyncProperties.getDiscord();
        return createThreadPoolTaskExecutor(
            config.getCorePoolSize(),
            config.getMaxPoolSize(),
            config.getQueueCapacity(),
            "discord-"
        );
    }

    private ThreadPoolTaskExecutor createThreadPoolTaskExecutor(
            int corePoolSize, int maxPoolSize, int queueCapacity, 
            String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async execution failed in {}.{} with params: {}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName(),
                    Arrays.toString(params), ex
            );
        };
    }
}
