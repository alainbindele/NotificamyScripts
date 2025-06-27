package com.notifyme.infrastructure.adapter.inbound.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler for the notification poller batch job
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduling.poller.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationPollerScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job notificationPollerJob;
    
    @Value("${scheduling.poller.enabled:true}")
    private boolean schedulingEnabled;
    
    @Scheduled(cron = "${scheduling.poller.cron:0 */1 * * * *}")
    public void runNotificationPollerJob() {
        if (!schedulingEnabled) {
            log.debug("Notification poller scheduling is disabled");
            return;
        }
        
        try {
            log.info("🚀 Starting notification poller job at {}", LocalDateTime.now());
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(notificationPollerJob, jobParameters);
            
            log.info("✅ Notification poller job completed successfully");
            
        } catch (Exception e) {
            log.error("❌ Failed to run notification poller job", e);
        }
    }
}