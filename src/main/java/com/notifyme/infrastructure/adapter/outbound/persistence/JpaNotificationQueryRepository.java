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
                   q.user_id, q.is_valid, q.closed, q.valid_from, q.valid_to,
                   u.email, u.discord_webhook, u.slack_webhook, u.whatsapp_phone
            FROM queries q 
            INNER JOIN users u ON q.user_id = u.id
            WHERE q.is_valid = 1 
              AND (q.next_execution <= NOW() OR q.next_execution IS NULL)
              AND q.closed = 0
              AND (q.valid_from IS NULL OR q.valid_from <= NOW())
              AND (q.valid_to IS NULL OR q.valid_to >= NOW())
            ORDER BY q.id ASC
            LIMIT ? OFFSET ?
            """;
        
        return jdbcTemplate.query(sql, new NotificationQueryRowMapper(), pageSize, offset);
    }
    
    @Override
    public void updateNextExecution(Long queryId, LocalDateTime nextExecution) {
        String sql = "UPDATE queries SET next_execution = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, nextExecution, queryId);
        
        if (updated > 0) {
            log.debug("Updated next_execution for query ID: {} to {}", queryId, nextExecution);
        } else {
            log.warn("No query found with ID: {} for next_execution update", queryId);
        }
    }
    
    @Override
    public long countQueriesDueForExecution() {
        String sql = """
            SELECT COUNT(*) 
            FROM queries q 
            WHERE q.is_valid = 1 
              AND (q.next_execution <= NOW() OR q.next_execution IS NULL)
              AND q.closed = 0
              AND (q.valid_from IS NULL OR q.valid_from <= NOW())
              AND (q.valid_to IS NULL OR q.valid_to >= NOW())
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
                .validFrom(rs.getTimestamp("valid_from") != null ? 
                    rs.getTimestamp("valid_from").toLocalDateTime() : null)
                .validTo(rs.getTimestamp("valid_to") != null ? 
                    rs.getTimestamp("valid_to").toLocalDateTime() : null)
                .userEmail(rs.getString("email"))
                .discordWebhook(rs.getString("discord_webhook"))
                .slackWebhook(rs.getString("slack_webhook"))
                .whatsappPhone(rs.getString("whatsapp_phone"))
                .build();
        }
    }
}