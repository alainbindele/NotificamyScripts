package com.notifyme.batch.processor;

import com.notifyme.model.NotificationQuery;
import com.notifyme.model.SQSNotificationMessage;
import com.notifyme.service.CronCalculatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Processes notification queries and converts them to SQS messages
 */
@Component
public class NotificationQueryProcessor implements ItemProcessor<NotificationQuery, SQSNotificationMessage> {

    private static final Logger logger = LoggerFactory.getLogger(NotificationQueryProcessor.class);

    @Autowired
    private CronCalculatorService cronCalculatorService;

    @Override
    public SQSNotificationMessage process(NotificationQuery query) throws Exception {
        logger.debug("Processing query ID: {} with prompt: {}", query.getId(), query.getPrompt());

        // Skip closed queries
        if (Boolean.TRUE.equals(query.getClosed())) {
            logger.info("⏭  Skipping Query ID {}: query is closed", query.getId());
            return null; // Returning null skips this item
        }

        // Validate required fields
        if (query.getPrompt() == null || query.getPrompt().trim().isEmpty() ||
            query.getUserEmail() == null || query.getUserEmail().trim().isEmpty()) {
            logger.warn("Skipping query ID {} due to missing prompt or email", query.getId());
            return null;
        }

        logger.info("▶️  Processing Query ID {}: {}", query.getId(), query.getPrompt());

        // Calculate next execution time if cron parameters are provided
        if (query.getCronParams() != null && !query.getCronParams().trim().isEmpty() && 
            !"NULL".equals(query.getCronParams())) {
            
            LocalDateTime baseTime = query.getNextExecution() != null ? 
                query.getNextExecution() : query.getCreatedAt();
            
            LocalDateTime nextExecution = cronCalculatorService.calculateNextExecution(
                query.getCronParams(), baseTime);
            
            query.setNextExecution(nextExecution);
            logger.info("⏭  Next execution for Query ID {}: {}", query.getId(), nextExecution);
        }

        // Build SQS message
        SQSNotificationMessage message = SQSNotificationMessage.builder()
            .queryId(query.getId())
            .userEmail(query.getUserEmail())
            .prompt(query.getPrompt())
            .userDiscordWebhook(query.getDiscordWebhook())
            .userSlackWebhook(query.getSlackWebhook())
            .userPhone(query.getWhatsappPhone())
            .build();

        logger.debug("Created SQS message for query ID: {}", query.getId());
        return message;
    }
}