package com.kafka.hubordereventpushing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j(topic = "THREADPOOL-CONFIG")
public class ThreadPoolConfig {

    private ThreadPoolTaskExecutor executor;

    @Bean(name = "customExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {

        executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);        // số luồng tối thiểu
        executor.setMaxPoolSize(10);        // số luồng tối đa
        executor.setQueueCapacity(50);          // số task có thể chờ
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("BatchThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()  );
        // CallerRunsPolicy = chạy ngay task đó bằng thread hiện tại (thread gọi submit)
        executor.initialize();

        return executor;
    }

    @Bean(name = "telegramExecutor")
    public ThreadPoolTaskExecutor telegramExecutor() {

        executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);        // số luồng tối thiểu
        executor.setMaxPoolSize(5);        // số luồng tối đa
        executor.setQueueCapacity(50);          // số task có thể chờ
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("TeleThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()  );
        // CallerRunsPolicy = chạy ngay task đó bằng thread hiện tại (thread gọi submit)
        executor.initialize();

        return executor;
    }

}
