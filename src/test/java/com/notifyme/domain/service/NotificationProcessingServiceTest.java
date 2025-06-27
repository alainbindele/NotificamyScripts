package com.notifyme.domain.service;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.port.outbound.CronCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationProcessingServiceTest {

    @Mock
    private CronCalculatorService cronCalculatorService;

    @InjectMocks
    private NotificationProcessingService processingService;

    private NotificationQuery testQuery;

    @BeforeEach
    void setUp() {
        testQuery = NotificationQuery.builder()
            .id(1L)
            .prompt("Test notification")
            .userEmail("test@example.com")
            .cronParams("0 */5 * * * *")
            .createdAt(LocalDateTime.now())
            .closed(false)
            .isValid(true)
            .build();
    }

    @Test
    void testProcessQuery_ValidQuery() {
        when(cronCalculatorService.calculateNextExecution(anyString(), any(LocalDateTime.class)))
            .thenReturn(LocalDateTime.now().plusMinutes(5));

        NotificationMessage result = processingService.processQuery(testQuery);

        assertNotNull(result);
        assertEquals(testQuery.getId(), result.getQueryId());
        assertEquals(testQuery.getUserEmail(), result.getUserEmail());
        assertEquals(testQuery.getPrompt(), result.getPrompt());
    }

    @Test
    void testProcessQuery_ClosedQuery() {
        testQuery.setClosed(true);

        NotificationMessage result = processingService.processQuery(testQuery);

        assertNull(result); // Should skip closed queries
    }

    @Test
    void testProcessQuery_EmptyPrompt() {
        testQuery.setPrompt("");

        NotificationMessage result = processingService.processQuery(testQuery);

        assertNull(result); // Should skip queries with empty prompt
    }

    @Test
    void testProcessQuery_EmptyEmail() {
        testQuery.setUserEmail("");

        NotificationMessage result = processingService.processQuery(testQuery);

        assertNull(result); // Should skip queries with empty email
    }

    @Test
    void testProcessQuery_WithOptionalFields() {
        testQuery.setDiscordWebhook("https://discord.com/webhook");
        testQuery.setSlackWebhook("https://slack.com/webhook");
        testQuery.setWhatsappPhone("+1234567890");

        when(cronCalculatorService.calculateNextExecution(anyString(), any(LocalDateTime.class)))
            .thenReturn(LocalDateTime.now().plusMinutes(5));

        NotificationMessage result = processingService.processQuery(testQuery);

        assertNotNull(result);
        assertEquals("https://discord.com/webhook", result.getUserDiscordWebhook());
        assertEquals("https://slack.com/webhook", result.getUserSlackWebhook());
        assertEquals("+1234567890", result.getUserPhone());
    }
}