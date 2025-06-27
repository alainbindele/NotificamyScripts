package com.notifyme.batch.processor;

import com.notifyme.model.NotificationQuery;
import com.notifyme.model.SQSNotificationMessage;
import com.notifyme.service.CronCalculatorService;
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
class NotificationQueryProcessorTest {

    @Mock
    private CronCalculatorService cronCalculatorService;

    @InjectMocks
    private NotificationQueryProcessor processor;

    private NotificationQuery testQuery;

    @BeforeEach
    void setUp() {
        testQuery = new NotificationQuery();
        testQuery.setId(1L);
        testQuery.setPrompt("Test notification");
        testQuery.setUserEmail("test@example.com");
        testQuery.setCronParams("0 */5 * * * *");
        testQuery.setCreatedAt(LocalDateTime.now());
        testQuery.setClosed(false);
    }

    @Test
    void testProcess_ValidQuery() throws Exception {
        when(cronCalculatorService.calculateNextExecution(anyString(), any(LocalDateTime.class)))
            .thenReturn(LocalDateTime.now().plusMinutes(5));

        SQSNotificationMessage result = processor.process(testQuery);

        assertNotNull(result);
        assertEquals(testQuery.getId(), result.getQueryId());
        assertEquals(testQuery.getUserEmail(), result.getUserEmail());
        assertEquals(testQuery.getPrompt(), result.getPrompt());
    }

    @Test
    void testProcess_ClosedQuery() throws Exception {
        testQuery.setClosed(true);

        SQSNotificationMessage result = processor.process(testQuery);

        assertNull(result); // Should skip closed queries
    }

    @Test
    void testProcess_EmptyPrompt() throws Exception {
        testQuery.setPrompt("");

        SQSNotificationMessage result = processor.process(testQuery);

        assertNull(result); // Should skip queries with empty prompt
    }

    @Test
    void testProcess_EmptyEmail() throws Exception {
        testQuery.setUserEmail("");

        SQSNotificationMessage result = processor.process(testQuery);

        assertNull(result); // Should skip queries with empty email
    }

    @Test
    void testProcess_WithOptionalFields() throws Exception {
        testQuery.setDiscordWebhook("https://discord.com/webhook");
        testQuery.setSlackWebhook("https://slack.com/webhook");
        testQuery.setWhatsappPhone("+1234567890");

        when(cronCalculatorService.calculateNextExecution(anyString(), any(LocalDateTime.class)))
            .thenReturn(LocalDateTime.now().plusMinutes(5));

        SQSNotificationMessage result = processor.process(testQuery);

        assertNotNull(result);
        assertEquals("https://discord.com/webhook", result.getUserDiscordWebhook());
        assertEquals("https://slack.com/webhook", result.getUserSlackWebhook());
        assertEquals("+1234567890", result.getUserPhone());
    }
}