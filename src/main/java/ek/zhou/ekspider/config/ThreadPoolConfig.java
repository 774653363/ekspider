package ek.zhou.ekspider.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {
    @Bean(name = "crawlExecutorPool")
    public ExecutorService crawlExecutorPool() {
        // 获取Java虚拟机的可用的处理器数，最佳线程个数，处理器数*2。根据实际情况调整
        int curSystemThreads = Runtime.getRuntime().availableProcessors() * 2;
        System.out.println("------------系统可用线程池个数：" + curSystemThreads);
        // 创建线程池
        ExecutorService pool = new ThreadPoolExecutor(1,curSystemThreads,60L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        return pool;
    }
}
