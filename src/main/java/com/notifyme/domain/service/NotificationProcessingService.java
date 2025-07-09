package com.notifyme.domain.service;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.port.outbound.CronCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Domain service for processing notification queries
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProcessingService {
    
    private final CronCalculatorService cronCalculatorService;
    
    /**
     * Process a notification query and convert it to a message
     * 
     * @param query The query to process
     * @return NotificationMessage or null if query should be skipped
     */
    public NotificationMessage processQuery(NotificationQuery query) {
        log.debug("Processing query ID: {} with prompt: {}", query.getId(), query.getPrompt());
        
        // Skip closed queries
        if (query.isClosed()) {
            log.info("⏭  Skipping Query ID {}: query is closed", query.getId());
            return null;
        }
        
        // Check validity period
        if (!query.isCurrentlyValid()) {
            log.info("⏭  Skipping Query ID {}: query is outside validity period (valid_from: {}, valid_to: {})", 
                    query.getId(), query.getValidFrom(), query.getValidTo());
            return null;
        }
        
        // Validate required fields
        if (!query.hasValidPrompt() || !query.hasValidEmail()) {
            log.warn("Skipping query ID {} due to missing prompt or email", query.getId());
            return null;
        }
        
        log.info("▶️  Processing Query ID {}: {} (valid_from: {}, valid_to: {})", 
                query.getId(), query.getPrompt(), query.getValidFrom(), query.getValidTo());
        
        // Calculate next execution time if cron parameters are provided
        if (query.hasCronParams()) {
            LocalDateTime baseTime = query.getNextExecution() != null ? 
                query.getNextExecution() : query.getCreatedAt();
            
            LocalDateTime nextExecution = cronCalculatorService.calculateNextExecution(
                query.getCronParams(), baseTime);
            
            query.setNextExecution(nextExecution);
            log.info("⏭  Next execution for Query ID {}: {}", query.getId(), nextExecution);
        }
        
        // Convert to domain message
        NotificationMessage message = NotificationMessage.fromQuery(query);
        
        log.debug("Created notification message for query ID: {}", query.getId());
        return message;
    }
}