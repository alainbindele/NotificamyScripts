package com.notifyme.domain.service;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.port.outbound.CronCalculatorService;
import com.notifyme.domain.port.outbound.NotificationQueryRepository;
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
    private final NotificationQueryRepository queryRepository;
    
    /**
     * Process a notification query and convert it to a message
     * 
     * @param query The query to process
     * @return NotificationMessage or null if query should be skipped
     */
    public NotificationMessage processQuery(NotificationQuery query) {
        log.debug("Processing query ID: {} with prompt: {}", query.getId(), query.getPrompt());
        
        // Double-check: Skip closed queries (should already be filtered at DB level)
        if (query.isClosed()) {
            log.warn("⚠️  Query ID {} is closed but was not filtered at database level", query.getId());
            return null;
        }
        
        // Validate required fields
        if (!query.hasValidPrompt() || !query.hasValidEmail()) {
            log.warn("⚠️  Skipping query ID {} due to missing prompt or email", query.getId());
            return null;
        }
        
        log.info("▶️  Processing Query ID {}: {}", query.getId(), query.getPrompt());
        
        // Calculate and update next execution time if cron parameters are provided
        if (query.hasCronParams()) {
            LocalDateTime baseTime = query.getNextExecution() != null ? 
                query.getNextExecution() : query.getCreatedAt();
            
            LocalDateTime nextExecution = cronCalculatorService.calculateNextExecution(
                query.getCronParams(), baseTime);
            
            // Update next execution in database immediately
            try {
                queryRepository.updateNextExecution(query.getId(), nextExecution);
                query.setNextExecution(nextExecution);
                log.info("⏭  Updated next execution for Query ID {} to: {}", query.getId(), nextExecution);
            } catch (Exception e) {
                log.error("Failed to update next execution for query ID: {}", query.getId(), e);
                // Continue processing even if update fails
            }
        }
        
        // Convert to domain message
        NotificationMessage message = NotificationMessage.fromQuery(query);
        
        log.debug("✅ Created notification message for query ID: {}", query.getId());
        return message;
    }
}