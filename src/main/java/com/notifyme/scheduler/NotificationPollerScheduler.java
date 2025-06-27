package com.notifyme.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduler for the notification poller batch job
 */
@Component
@ConditionalOnProperty(name = "scheduling.poller.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationPollerScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPollerScheduler.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job notificationPollerJob;

    @Value("${scheduling.poller.enabled:true}")
    private boolean schedulingEnabled;

    @Scheduled(cron = "${scheduling.poller.cron:0 */1 * * * *}")
    public void runNotificationPollerJob() {
        if (!schedulingEnabled) {
            logger.debug("Notification poller scheduling is disabled");
            return;
        }

        try {
            logger.info("🚀 Starting notification poller job at {}", LocalDateTime.now());

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(notificationPollerJob, jobParameters);

            logger.info("✅ Notification poller job completed successfully");

        } catch (Exception e) {
            logger.error("❌ Failed to run notification poller job", e);
        }
    }
}