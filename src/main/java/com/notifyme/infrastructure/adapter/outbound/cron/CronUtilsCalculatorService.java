package com.notifyme.infrastructure.adapter.outbound.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.notifyme.domain.port.outbound.CronCalculatorService;
import com.notifyme.infrastructure.config.TimezoneConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * CronUtils implementation of CronCalculatorService
 */
@Slf4j
@Service
public class CronUtilsCalculatorService implements CronCalculatorService {
    
    private final CronParser cronParser;
    private final TimezoneConfig timezoneConfig;
    
    @Autowired
    public CronUtilsCalculatorService(TimezoneConfig timezoneConfig) {
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        this.timezoneConfig = timezoneConfig;
    }
    
    @Override
    public LocalDateTime calculateNextExecution(String cronExpression, LocalDateTime baseTime) {
        try {
            log.debug("Calculating next execution for cron: {} from base time: {}", 
                     cronExpression, baseTime);
            
            Cron cron = cronParser.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            
            // Use configured application timezone instead of system default
            ZonedDateTime baseZoned = baseTime.atZone(timezoneConfig.getApplicationZoneId());
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(baseZoned);
            
            if (nextExecution.isPresent()) {
                LocalDateTime result = nextExecution.get().toLocalDateTime();
                
                // Ensure the next execution is in the future
                LocalDateTime now = LocalDateTime.now();
                while (result.isBefore(now) || result.isEqual(now)) {
                    ZonedDateTime resultZoned = result.atZone(timezoneConfig.getApplicationZoneId());
                    Optional<ZonedDateTime> nextNext = executionTime.nextExecution(resultZoned);
                    if (nextNext.isPresent()) {
                        result = nextNext.get().toLocalDateTime();
                    } else {
                        break;
                    }
                }
                
                log.debug("Next execution calculated: {}", result);
                return result;
            } else {
                log.warn("Could not calculate next execution for cron: {}", cronExpression);
                return baseTime.plusMinutes(5); // Fallback: 5 minutes from base time
            }
            
        } catch (Exception e) {
            log.error("Error calculating next execution for cron: {} from base time: {}", 
                     cronExpression, baseTime, e);
            return baseTime.plusMinutes(5); // Fallback: 5 minutes from base time
        }
    }
    
    @Override
    public boolean isValidCronExpression(String cronExpression) {
        try {
            cronParser.parse(cronExpression);
            return true;
        } catch (Exception e) {
            log.debug("Invalid cron expression: {}", cronExpression, e);
            return false;
        }
    }
}