package com.notifyme.infrastructure.adapter.inbound.batch.writer;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.port.outbound.MessageQueueService;
import com.notifyme.domain.port.outbound.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring Batch ItemWriter for notification messages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageItemWriter implements ItemWriter<NotificationMessage> {
    
    private final MessageQueueService messageQueueService;
    private final NotificationQueryRepository queryRepository;
    
    @Override
    public void write(Chunk<? extends NotificationMessage> chunk) throws Exception {
        List<? extends NotificationMessage> messages = chunk.getItems();
        
        if (messages.isEmpty()) {
            return;
        }
        
        log.info("Writing {} messages to message queue", messages.size());
        
        try {
            messageQueueService.sendMessages(List.copyOf(messages));
            log.info("Successfully sent {} messages to message queue", messages.size());
            
            // Update next_execution for queries with cron parameters
            updateNextExecutionTimes(messages);
            
        } catch (MessageQueueService.MessageQueueException e) {
            log.error("Failed to send messages to message queue", e);
            throw new RuntimeException("Failed to send messages to message queue", e);
        }
    }
    
    private void updateNextExecutionTimes(List<? extends NotificationMessage> messages) {
        for (NotificationMessage message : messages) {
            if (message.getNextExecution() != null) {
                queryRepository.updateNextExecution(message.getQueryId(), message.getNextExecution());
                log.debug("Updated next_execution for query ID: {} to {}", 
                         message.getQueryId(), message.getNextExecution());
            }
        }
    }
}