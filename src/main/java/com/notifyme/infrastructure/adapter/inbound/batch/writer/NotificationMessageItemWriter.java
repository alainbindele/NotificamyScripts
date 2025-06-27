package com.notifyme.infrastructure.adapter.inbound.batch.writer;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.port.outbound.MessageQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Spring Batch ItemWriter for notification messages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageItemWriter implements ItemWriter<NotificationMessage> {
    
    private final MessageQueueService messageQueueService;
    
    @Override
    public void write(Chunk<? extends NotificationMessage> chunk) throws Exception {
        // Filter out null messages (from closed/skipped queries)
        List<NotificationMessage> validMessages = chunk.getItems().stream()
                .filter(Objects::nonNull)
                .map(NotificationMessage.class::cast)
                .toList();
        
        if (validMessages.isEmpty()) {
            log.debug("No valid messages to write (all were filtered out)");
            return;
        }
        
        log.info("Writing {} valid messages to message queue (filtered from {} total)", 
                validMessages.size(), chunk.getItems().size());
        
        try {
            messageQueueService.sendMessages(validMessages);
            log.info("Successfully sent {} messages to message queue", validMessages.size());
        } catch (MessageQueueService.MessageQueueException e) {
            log.error("Failed to send messages to message queue", e);
            throw new RuntimeException("Failed to send messages to message queue", e);
        }
    }
}