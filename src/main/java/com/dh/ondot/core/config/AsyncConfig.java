package com.dh.ondot.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

import static com.dh.ondot.core.config.AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR;

@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Value("${async.event.core-pool-size}")
    private int corePoolSize;
    @Value("${async.event.max-pool-size}")
    private int maxPoolSize;
    @Value("${async.event.queue-capacity}")
    private int queueCapacity;

    @Bean(name = EVENT_ASYNC_TASK_EXECUTOR)
    public Executor eventAsyncExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(corePoolSize);
        ex.setMaxPoolSize(maxPoolSize);
        ex.setQueueCapacity(queueCapacity);
        ex.setThreadNamePrefix("event-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();
        return ex;
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
