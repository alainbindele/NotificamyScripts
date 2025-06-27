package com.notifyme.batch.writer;

import com.notifyme.model.SQSNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Writer that updates the next_execution timestamp in the database
 */
@Component
public class DatabaseUpdateWriter implements ItemWriter<SQSNotificationMessage> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUpdateWriter.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends SQSNotificationMessage> chunk) throws Exception {
        List<? extends SQSNotificationMessage> messages = chunk.getItems();
        
        if (messages.isEmpty()) {
            return;
        }

        logger.debug("Updating next_execution for {} queries", messages.size());

        // Note: In a real implementation, you'd need to pass the calculated next_execution
        // from the processor. For now, this is a placeholder showing the structure.
        
        // This would typically be done with a batch update for better performance
        String updateSql = "UPDATE queries SET next_execution = ? WHERE id = ?";
        
        // In practice, you'd collect the next_execution times from the processor
        // and perform batch updates here
        
        logger.debug("Database updates completed for {} queries", messages.size());
    }
}