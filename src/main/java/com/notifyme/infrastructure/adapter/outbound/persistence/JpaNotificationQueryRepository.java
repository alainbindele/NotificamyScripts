package com.notifyme.infrastructure.adapter.outbound.persistence;

import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.domain.port.outbound.NotificationQueryRepository;
import com.notifyme.infrastructure.adapter.outbound.persistence.mapper.NotificationQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA implementation of NotificationQueryRepository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaNotificationQueryRepository implements NotificationQueryRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final NotificationQueryMapper mapper;
    
    @Override
    public List<NotificationQuery> findQueriesDueForExecution(int pageSize, int offset) {
        String sql = """
            SELECT q.id, q.prompt, q.cron_params, q.next_execution, q.created_at, 
                   q.user_id, q.is_valid, q.closed,
                   u.email, u.discord_webhook, u.slack_webhook, u.whatsapp_phone
            FROM queries q 
            INNER JOIN users u ON q.user_id = u.id
            WHERE q.is_valid = 1 
              AND q.closed = 0
              AND (
                  (q.next_execution IS NULL AND q.cron_params IS NOT NULL AND q.cron_params != '' AND q.cron_params != 'NULL') OR
                  (q.next_execution <= NOW() AND q.cron_params IS NOT NULL AND q.cron_params != '' AND q.cron_params != 'NULL') OR
                  (q.cron_params IS NULL OR q.cron_params = '' OR q.cron_params = 'NULL')
              )
            ORDER BY q.id ASC
            LIMIT ? OFFSET ?
            """;
        
        List<NotificationQuery> queries = jdbcTemplate.query(sql, new NotificationQueryRowMapper(), pageSize, offset);
        
        log.info("Found {} queries due for execution (offset: {}, pageSize: {})", queries.size(), offset, pageSize);
        
        // Log details for debugging
        for (NotificationQuery query : queries) {
            log.info("Query ID {}: cron_params='{}', next_execution={}, closed={}", 
                    query.getId(), query.getCronParams(), query.getNextExecution(), query.isClosed());
        }
        
        return queries;
    }
    
    @Override
    public void updateNextExecution(Long queryId, LocalDateTime nextExecution) {
        String sql = "UPDATE queries SET next_execution = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, nextExecution, queryId);
        
        if (updated > 0) {
            log.info("✅ Updated next_execution for query ID: {} to {}", queryId, nextExecution);
        } else {
            log.warn("⚠️  No query found with ID: {} for next_execution update", queryId);
        }
    }
    
    @Override
    public long countQueriesDueForExecution() {
        String sql = """
            SELECT COUNT(*) 
            FROM queries q 
            WHERE q.is_valid = 1 
              AND q.closed = 0
              AND (
                  (q.next_execution IS NULL AND q.cron_params IS NOT NULL AND q.cron_params != '' AND q.cron_params != 'NULL') OR
                  (q.next_execution <= NOW() AND q.cron_params IS NOT NULL AND q.cron_params != '' AND q.cron_params != 'NULL') OR
                  (q.cron_params IS NULL OR q.cron_params = '' OR q.cron_params = 'NULL')
              )
            """;
        
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
    
    /**
     * Row mapper for NotificationQuery with user information
     */
    private static class NotificationQueryRowMapper implements RowMapper<NotificationQuery> {
        
        @Override
        public NotificationQuery mapRow(ResultSet rs, int rowNum) throws SQLException {
            return NotificationQuery.builder()
                .id(rs.getLong("id"))
                .prompt(rs.getString("prompt"))
                .cronParams(rs.getString("cron_params"))
                .nextExecution(rs.getTimestamp("next_execution") != null ? 
                    rs.getTimestamp("next_execution").toLocalDateTime() : null)
                .createdAt(rs.getTimestamp("created_at") != null ? 
                    rs.getTimestamp("created_at").toLocalDateTime() : null)
                .userId(rs.getLong("user_id"))
                .isValid(rs.getBoolean("is_valid"))
                .closed(rs.getBoolean("closed"))
                .userEmail(rs.getString("email"))
                .discordWebhook(rs.getString("discord_webhook"))
                .slackWebhook(rs.getString("slack_webhook"))
                .whatsappPhone(rs.getString("whatsapp_phone"))
                .build();
        }
    }
}