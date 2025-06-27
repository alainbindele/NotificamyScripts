package com.notifyme.application.service;

import com.notifyme.domain.port.inbound.NotificationPollerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

/**
 * Application service implementing notification poller use cases
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPollerApplicationService implements NotificationPollerUseCase {
    
    private final JobLauncher jobLauncher;
    private final Job notificationPollerJob;
    
    @Override
    public Long triggerPollerJob() throws NotificationPollerException {
        try {
            log.info("Manually triggering notification poller job");
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("trigger", "manual")
                    .toJobParameters();
            
            var jobExecution = jobLauncher.run(notificationPollerJob, jobParameters);
            
            log.info("Notification poller job triggered with execution ID: {}", 
                    jobExecution.getId());
            
            return jobExecution.getId();
            
        } catch (Exception e) {
            log.error("Failed to trigger notification poller job", e);
            throw new NotificationPollerException("Failed to trigger notification poller job", e);
        }
    }
}