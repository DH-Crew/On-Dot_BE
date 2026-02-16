package com.dh.ondot.core.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class AsyncConfig(
    private val asyncProperties: AsyncProperties,
) : AsyncConfigurer {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean(AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR)
    fun eventAsyncExecutor(): Executor {
        val config = asyncProperties.event
        return createThreadPoolTaskExecutor(
            config.corePoolSize,
            config.maxPoolSize,
            config.queueCapacity,
            "event-",
        )
    }

    @Bean(AsyncConstants.DISCORD_ASYNC_TASK_EXECUTOR)
    fun discordAsyncExecutor(): Executor {
        val config = asyncProperties.discord
        return createThreadPoolTaskExecutor(
            config.corePoolSize,
            config.maxPoolSize,
            config.queueCapacity,
            "discord-",
        )
    }

    private fun createThreadPoolTaskExecutor(
        corePoolSize: Int,
        maxPoolSize: Int,
        queueCapacity: Int,
        threadNamePrefix: String,
    ): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = corePoolSize
        executor.maxPoolSize = maxPoolSize
        executor.queueCapacity = queueCapacity
        executor.setThreadNamePrefix(threadNamePrefix)
        executor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(10)
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { ex, method, params ->
            log.error(
                "Async execution failed in {}.{} with params: {}",
                method.declaringClass.simpleName,
                method.name,
                params.contentToString(),
                ex,
            )
        }
    }
}
