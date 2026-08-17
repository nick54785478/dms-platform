package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <b>非同步執行緒池設定</b>
 * <p>
 * 取代 Spring 預設的 SimpleAsyncTaskExecutor (每任務一執行緒) 或 ForkJoinPool，
 * 避免在高併發 I/O 操作 (如 MinIO 大量刪除/複製) 時產生 OOM (Out Of Memory)。
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "minioAsyncExecutor")
    public Executor minioAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 核心執行緒數
        executor.setMaxPoolSize(50);  // 最大執行緒數
        executor.setQueueCapacity(500); // 佇列容量，若滿了則觸發拒絕策略
        executor.setThreadNamePrefix("MinIO-Async-");
        
        // 拒絕策略：當佇列滿了且執行緒達上限時，由呼叫者(主執行緒)自行執行，避免任務遺失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        return executor;
    }
}
