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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch ItemReader for notification queries with duplicate prevention
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueryItemReader implements ItemReader<NotificationQuery> {
    
    private final NotificationQueryRepository repository;
    
    @Value("${batch.notification-poller.chunk-size}")
    private int chunkSize;
    
    private Iterator<NotificationQuery> queryIterator;
    private final AtomicInteger currentOffset = new AtomicInteger(0);
    private volatile boolean hasMoreData = true;
    private volatile boolean initialized = false;
    
    // Track processed queries to prevent duplicates
    private final ConcurrentHashMap<Long, Boolean> processedQueries = new ConcurrentHashMap<>();
    
    @Override
    public synchronized NotificationQuery read() throws Exception {
        if (!initialized) {
            reset();
            initialized = true;
        }
        
        if (queryIterator == null || (!queryIterator.hasNext() && hasMoreData)) {
            loadNextBatch();
        }
        
        if (queryIterator != null && queryIterator.hasNext()) {
            NotificationQuery query = queryIterator.next();
            
            // Check if already processed to prevent duplicates
            if (processedQueries.putIfAbsent(query.getId(), true) != null) {
                log.debug("Skipping already processed query ID: {}", query.getId());
                return read(); // Recursively get next query
            }
            
            log.debug("Reading query ID: {} - {}", query.getId(), query.getPrompt());
            return query;
        }
        
        // End of data - reset for next job execution
        log.info("Finished reading all queries. Processed {} unique queries", processedQueries.size());
        reset();
        return null;
    }
    
    private void loadNextBatch() {
        int offset = currentOffset.get();
        log.debug("Loading next batch of queries, offset: {}, size: {}", offset, chunkSize);
        
        List<NotificationQuery> queries = repository.findQueriesDueForExecution(chunkSize, offset);
        
        if (queries.isEmpty()) {
            hasMoreData = false;
            queryIterator = null;
            log.debug("No more queries to process at offset: {}", offset);
        } else {
            queryIterator = queries.iterator();
            currentOffset.addAndGet(queries.size());
            hasMoreData = queries.size() == chunkSize;
            log.debug("Loaded {} queries, new offset: {}, hasMoreData: {}", 
                     queries.size(), currentOffset.get(), hasMoreData);
        }
    }
    
    private void reset() {
        queryIterator = null;
        currentOffset.set(0);
        hasMoreData = true;
        processedQueries.clear();
        initialized = false;
        log.debug("Reader reset for next job execution");
    }
}