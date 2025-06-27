package com.notifyme.infrastructure.adapter.inbound.batch.config;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.model.NotificationQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration for notification poller
 */
@Configuration
@RequiredArgsConstructor
public class NotificationPollerBatchConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    @Value("${batch.notification-poller.chunk-size}")
    private int chunkSize;
    
    @Value("${batch.notification-poller.skip-limit}")
    private int skipLimit;
    
    @Value("${batch.notification-poller.retry-limit}")
    private int retryLimit;
    
    @Value("${batch.notification-poller.thread-pool-size}")
    private int threadPoolSize;
    
    @Value("${batch.notification-poller.throttle-limit}")
    private int throttleLimit;
    
    @Bean
    public TaskExecutor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("notification-batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    @Bean
    public Step notificationPollerStep(
            ItemReader<NotificationQuery> notificationQueryItemReader,
            ItemProcessor<NotificationQuery, NotificationMessage> notificationQueryItemProcessor,
            ItemWriter<NotificationMessage> notificationMessageItemWriter) {
        
        return new StepBuilder("notificationPollerStep", jobRepository)
                .<NotificationQuery, NotificationMessage>chunk(chunkSize, transactionManager)
                .reader(notificationQueryItemReader)
                .processor(notificationQueryItemProcessor)
                .writer(notificationMessageItemWriter)
                .faultTolerant()
                .skipLimit(skipLimit)
                .skip(Exception.class)
                .retryLimit(retryLimit)
                .retry(Exception.class)
                // Disable multi-threading to prevent duplicate processing
                // .taskExecutor(notificationTaskExecutor())
                // .throttleLimit(throttleLimit)
                .build();
    }
    
    @Bean
    public Job notificationPollerJob(Step notificationPollerStep) {
        return new JobBuilder("notificationPollerJob", jobRepository)
                .start(notificationPollerStep)
                .build();
    }
}