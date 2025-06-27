package com.notifyme.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for manually triggering the notification poller job
 */
@Service
public class NotificationPollerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPollerService.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job notificationPollerJob;

    /**
     * Manually trigger the notification poller job
     * 
     * @return Job execution ID
     */
    public Long triggerPollerJob() {
        try {
            logger.info("Manually triggering notification poller job");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("trigger", "manual")
                    .toJobParameters();

            var jobExecution = jobLauncher.run(notificationPollerJob, jobParameters);
            
            logger.info("Notification poller job triggered with execution ID: {}", 
                       jobExecution.getId());
            
            return jobExecution.getId();

        } catch (Exception e) {
            logger.error("Failed to trigger notification poller job", e);
            throw new RuntimeException("Failed to trigger notification poller job", e);
        }
    }
}