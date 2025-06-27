package com.notifyme.infrastructure.adapter.inbound.batch.reader;

import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.port.outbound.NotificationQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Spring Batch ItemReader for notification queries
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueryItemReader implements ItemReader<NotificationQuery> {
    
    private final NotificationQueryRepository repository;
    
    @Value("${batch.notification-poller.chunk-size}")
    private int chunkSize;
    
    private Iterator<NotificationQuery> queryIterator;
    private int currentOffset = 0;
    private boolean hasMoreData = true;
    
    @Override
    public NotificationQuery read() throws Exception {
        if (queryIterator == null || (!queryIterator.hasNext() && hasMoreData)) {
            loadNextBatch();
        }
        
        if (queryIterator != null && queryIterator.hasNext()) {
            return queryIterator.next();
        }
        
        // Reset for next job execution
        reset();
        return null;
    }
    
    private void loadNextBatch() {
        log.debug("Loading next batch of queries, offset: {}, size: {}", currentOffset, chunkSize);
        
        List<NotificationQuery> queries = repository.findQueriesDueForExecution(chunkSize, currentOffset);
        
        if (queries.isEmpty()) {
            hasMoreData = false;
            queryIterator = null;
            log.debug("No more queries to process");
        } else {
            queryIterator = queries.iterator();
            currentOffset += queries.size();
            hasMoreData = queries.size() == chunkSize;
            log.debug("Loaded {} queries, hasMoreData: {}", queries.size(), hasMoreData);
        }
    }
    
    private void reset() {
        queryIterator = null;
        currentOffset = 0;
        hasMoreData = true;
    }
}