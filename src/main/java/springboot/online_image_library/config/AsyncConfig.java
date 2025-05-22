package springboot.online_image_library.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Yu'S'hui'shen
 * @description 异步线程池配置，为不同领域服务提供独立的线程池
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 为图片相关异步任务（如存储桶删除）配置线程池
     */
    @Bean(name = "imageAsyncExecutor")
    public Executor imageAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 任务队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀，便于调试
        executor.setThreadNamePrefix("Image-Async-");
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("图片线程池任务被拒绝，队列已满，任务: {}", r);
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, e);
        });
        executor.initialize();
        return executor;
    }

    /**
     * 为日志记录相关异步任务配置线程池
     */
    @Bean(name = "logAsyncExecutor")
    public Executor logAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 日志任务较轻量，核心线程数较小
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(5);
        // 队列容量
        executor.setQueueCapacity(50);
        // 线程名前缀
        executor.setThreadNamePrefix("Log-Async-");
        // 拒绝策略：丢弃任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
}