package com.notifyme.domain.port.outbound;

import com.notifyme.domain.model.NotificationQuery;

import java.util.List;

/**
 * Port for notification query repository operations
 */
public interface NotificationQueryRepository {
    
    /**
     * Find queries that are due for execution
     * 
     * @param pageSize Number of queries to fetch
     * @param offset Offset for pagination
     * @return List of queries due for execution
     */
    List<NotificationQuery> findQueriesDueForExecution(int pageSize, int offset);
    
    /**
     * Update the next execution time for a query
     * 
     * @param queryId The query ID
     * @param nextExecution The next execution time
     */
    void updateNextExecution(Long queryId, java.time.LocalDateTime nextExecution);
    
    /**
     * Count total queries due for execution
     * 
     * @return Total count
     */
    long countQueriesDueForExecution();
}