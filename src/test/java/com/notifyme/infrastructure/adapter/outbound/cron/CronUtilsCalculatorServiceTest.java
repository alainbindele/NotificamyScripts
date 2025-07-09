package com.notifyme.infrastructure.adapter.outbound.cron;

import com.notifyme.infrastructure.config.TimezoneConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(MockitoExtension.class)
class CronUtilsCalculatorServiceTest {

    @Mock
    private TimezoneConfig timezoneConfig;
    
    private CronUtilsCalculatorService cronCalculatorService;
    
    void setUp() {
        when(timezoneConfig.getApplicationZoneId()).thenReturn(ZoneId.of("UTC"));
        cronCalculatorService = new CronUtilsCalculatorService(timezoneConfig);
    }

    @Test
    void testCalculateNextExecution_EveryMinute() {
        setUp();
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