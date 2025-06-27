package com.notifyme.infrastructure.adapter.inbound.batch.processor;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Spring Batch ItemProcessor for notification queries
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueryItemProcessor implements ItemProcessor<NotificationQuery, NotificationMessage> {
    
    private final NotificationProcessingService processingService;
    
    @Override
    public NotificationMessage process(NotificationQuery query) throws Exception {
        return processingService.processQuery(query);
    }
}