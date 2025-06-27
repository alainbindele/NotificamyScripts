package com.notifyme.infrastructure.adapter.outbound.cron;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CronUtilsCalculatorServiceTest {

    private final CronUtilsCalculatorService cronCalculatorService = new CronUtilsCalculatorService();

    @Test
    void testCalculateNextExecution_EveryMinute() {
        String cronExpression = "0 * * * * *"; // Every minute
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        
        LocalDateTime nextExecution = cronCalculatorService.calculateNextExecution(cronExpression, baseTime);
        
        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(baseTime));
    }

    @Test
    void testCalculateNextExecution_Every5Minutes() {
        String cronExpression = "0 */5 * * * *"; // Every 5 minutes
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 30, 0);
        
        LocalDateTime nextExecution = cronCalculatorService.calculateNextExecution(cronExpression, baseTime);
        
        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(baseTime));
    }

    @Test
    void testIsValidCronExpression_Valid() {
        assertTrue(cronCalculatorService.isValidCronExpression("0 * * * * *"));
        assertTrue(cronCalculatorService.isValidCronExpression("0 */5 * * * *"));
        assertTrue(cronCalculatorService.isValidCronExpression("0 0 12 * * *"));
    }

    @Test
    void testIsValidCronExpression_Invalid() {
        assertFalse(cronCalculatorService.isValidCronExpression("invalid"));
        assertFalse(cronCalculatorService.isValidCronExpression(""));
        assertFalse(cronCalculatorService.isValidCronExpression("* * * *"));
    }
}