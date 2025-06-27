package com.notifyme.domain.port.outbound;

import java.time.LocalDateTime;

/**
 * Port for cron calculation operations
 */
public interface CronCalculatorService {
    
    /**
     * Calculate the next execution time from a cron expression
     * 
     * @param cronExpression The cron expression
     * @param baseTime The base time to calculate from
     * @return The next execution time
     */
    LocalDateTime calculateNextExecution(String cronExpression, LocalDateTime baseTime);
    
    /**
     * Validate if a cron expression is valid
     * 
     * @param cronExpression The cron expression to validate
     * @return true if valid, false otherwise
     */
    boolean isValidCronExpression(String cronExpression);
}