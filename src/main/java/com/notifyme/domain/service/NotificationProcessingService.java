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
        log.debug("🔄 Processing query ID: {} with prompt: {}", query.getId(), query.getPrompt());
        
        // CRITICAL: First check - Skip closed queries
        if (query.isClosed()) {
            log.warn("🚨 CRITICAL: Query ID {} is CLOSED - should NOT have been selected! Skipping.", query.getId());
            return null;
        }
        
        // Validate required fields
        if (!query.hasValidPrompt() || !query.hasValidEmail()) {
            log.warn("⚠️  Skipping query ID {} due to missing prompt or email", query.getId());
            return null;
        }
        
        log.info("▶️  Processing Query ID {}: {}", query.getId(), query.getPrompt());
        
        // Handle cron-based queries
        if (query.hasCronParams()) {
            LocalDateTime baseTime = LocalDateTime.now(); // Always use current time as base
            
            LocalDateTime nextExecution = cronCalculatorService.calculateNextExecution(
                query.getCronParams(), baseTime);
            
            // Update next execution in database immediately to prevent re-processing
            try {
                queryRepository.updateNextExecution(query.getId(), nextExecution);
                query.setNextExecution(nextExecution);
                log.info("⏭  Updated next execution for Query ID {} to: {}", query.getId(), nextExecution);
            } catch (Exception e) {
                log.error("❌ Failed to update next execution for query ID: {}", query.getId(), e);
                // Don't continue processing if we can't update the next execution
                // This prevents infinite loops
                return null;
            }
        } else {
            // For queries without cron params, set next_execution to far future to prevent re-processing
            LocalDateTime farFuture = LocalDateTime.now().plusYears(100);
            try {
                queryRepository.updateNextExecution(query.getId(), farFuture);
                log.info("🔒 Set far future execution for one-time Query ID {}", query.getId());
            } catch (Exception e) {
                log.error("❌ Failed to update execution time for one-time query ID: {}", query.getId(), e);
                return null;
            }
        }
        
        // Convert to domain message
        NotificationMessage message = NotificationMessage.fromQuery(query);
        
        log.info("✅ Created notification message for query ID: {}", query.getId());
        return message;
    }
}