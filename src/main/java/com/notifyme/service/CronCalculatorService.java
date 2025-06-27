package com.notifyme.service;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Service for calculating next execution times from cron expressions
 */
@Service
public class CronCalculatorService {

    private static final Logger logger = LoggerFactory.getLogger(CronCalculatorService.class);
    
    private final CronParser cronParser;

    public CronCalculatorService() {
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    }

    /**
     * Calculate the next execution time from a cron expression and base time
     * 
     * @param cronExpression The cron expression (e.g., "0 */5 * * * *")
     * @param baseTime The base time to calculate from
     * @return The next execution time
     */
    public LocalDateTime calculateNextExecution(String cronExpression, LocalDateTime baseTime) {
        try {
            logger.debug("Calculating next execution for cron: {} from base time: {}", 
                        cronExpression, baseTime);

            Cron cron = cronParser.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            
            ZonedDateTime baseZoned = baseTime.atZone(ZoneId.systemDefault());
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(baseZoned);
            
            if (nextExecution.isPresent()) {
                LocalDateTime result = nextExecution.get().toLocalDateTime();
                
                // Ensure the next execution is in the future
                LocalDateTime now = LocalDateTime.now();
                while (result.isBefore(now) || result.isEqual(now)) {
                    ZonedDateTime resultZoned = result.atZone(ZoneId.systemDefault());
                    Optional<ZonedDateTime> nextNext = executionTime.nextExecution(resultZoned);
                    if (nextNext.isPresent()) {
                        result = nextNext.get().toLocalDateTime();
                    } else {
                        break;
                    }
                }
                
                logger.debug("Next execution calculated: {}", result);
                return result;
            } else {
                logger.warn("Could not calculate next execution for cron: {}", cronExpression);
                return baseTime.plusMinutes(5); // Fallback: 5 minutes from base time
            }
            
        } catch (Exception e) {
            logger.error("Error calculating next execution for cron: {} from base time: {}", 
                        cronExpression, baseTime, e);
            return baseTime.plusMinutes(5); // Fallback: 5 minutes from base time
        }
    }

    /**
     * Validate if a cron expression is valid
     * 
     * @param cronExpression The cron expression to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidCronExpression(String cronExpression) {
        try {
            cronParser.parse(cronExpression);
            return true;
        } catch (Exception e) {
            logger.debug("Invalid cron expression: {}", cronExpression, e);
            return false;
        }
    }
}