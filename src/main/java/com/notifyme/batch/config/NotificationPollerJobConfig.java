package com.notifyme.batch.config;

import com.notifyme.batch.processor.NotificationQueryProcessor;
import com.notifyme.model.NotificationQuery;
import com.notifyme.model.SQSNotificationMessage;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration for the notification poller batch job
 */
@Configuration
public class NotificationPollerJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
            ItemProcessor<NotificationQuery, SQSNotificationMessage> notificationQueryProcessor,
            @Qualifier("compositeNotificationWriter") ItemWriter<SQSNotificationMessage> compositeNotificationWriter) {
        
        return new StepBuilder("notificationPollerStep", jobRepository)
                .<NotificationQuery, SQSNotificationMessage>chunk(chunkSize, transactionManager)
                .reader(notificationQueryItemReader)
                .processor(notificationQueryProcessor)
                .writer(compositeNotificationWriter)
                .faultTolerant()
                .skipLimit(skipLimit)
                .skip(Exception.class)
                .retryLimit(retryLimit)
                .retry(Exception.class)
                .taskExecutor(notificationTaskExecutor())
                .throttleLimit(throttleLimit)
                .build();
    }

    @Bean
    public Job notificationPollerJob(Step notificationPollerStep) {
        return new JobBuilder("notificationPollerJob", jobRepository)
                .start(notificationPollerStep)
                .build();
    }
}