package com.notifyme.domain.port.outbound;

import java.time.LocalDateTime;

/**
 * Port for cron calculation operations
 */
public interface CronCalculatorService {
    
    /**
     * Calculate the next execution time from a cron expression in a specific timezone
     * 
     * @param cronExpression The cron expression
     * @param baseTime The base time to calculate from
     * @param timezone The timezone to use for calculation
     * @return The next execution time
     */
    LocalDateTime calculateNextExecution(String cronExpression, LocalDateTime baseTime, java.time.ZoneId timezone);
    
    /**
     * Calculate the next execution time from a cron expression using UTC timezone
     * 
     * @param cronExpression The cron expression
     * @param baseTime The base time to calculate from
     * @return The next execution time
     */
    default LocalDateTime calculateNextExecution(String cronExpression, LocalDateTime baseTime) {
        return calculateNextExecution(cronExpression, baseTime, java.time.ZoneId.of("UTC"));
    }
    
    /**
     * Validate if a cron expression is valid
     * 
     * @param cronExpression The cron expression to validate
     * @return true if valid, false otherwise
     */
    boolean isValidCronExpression(String cronExpression);
}