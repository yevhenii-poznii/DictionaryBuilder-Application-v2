package com.kiskee.dictionarybuilder.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@EnableAsync
@EnableRetry
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(50);
        threadPoolTaskExecutor.setKeepAliveSeconds(1);
        threadPoolTaskExecutor.setThreadNamePrefix("TimerThread-");
        threadPoolTaskExecutor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(threadPoolTaskExecutor);
    }
}
