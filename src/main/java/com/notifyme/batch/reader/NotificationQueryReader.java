package com.notifyme.batch.reader;

import com.notifyme.model.NotificationQuery;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Reader for notification queries that are due for execution
 */
@Configuration
public class NotificationQueryReader {

    @Autowired
    private DataSource dataSource;

    @Value("${batch.notification-poller.chunk-size}")
    private int chunkSize;

    @Bean
    public JdbcPagingItemReader<NotificationQuery> notificationQueryItemReader() {
        JdbcPagingItemReader<NotificationQuery> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(chunkSize);
        reader.setRowMapper(new NotificationQueryRowMapper());
        reader.setQueryProvider(createQueryProvider());
        reader.setName("notificationQueryItemReader");
        
        return reader;
    }

    private MySqlPagingQueryProvider createQueryProvider() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        
        queryProvider.setSelectClause(
            "SELECT q.id, q.prompt, q.cron_params, q.next_execution, q.created_at, " +
            "u.email, u.discord_webhook, u.slack_webhook, u.whatsapp_phone, q.closed"
        );
        
        queryProvider.setFromClause("FROM queries q INNER JOIN users u ON q.user_id = u.id");
        
        queryProvider.setWhereClause(
            "WHERE q.is_valid = 1 AND (q.next_execution <= NOW() OR q.next_execution IS NULL)"
        );
        
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("q.id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        
        return queryProvider;
    }

    /**
     * Row mapper for NotificationQuery with user information
     */
    public static class NotificationQueryRowMapper implements RowMapper<NotificationQuery> {
        
        @Override
        public NotificationQuery mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationQuery query = new NotificationQuery();
            
            query.setId(rs.getLong("id"));
            query.setPrompt(rs.getString("prompt"));
            query.setCronParams(rs.getString("cron_params"));
            
            // Handle nullable LocalDateTime
            if (rs.getTimestamp("next_execution") != null) {
                query.setNextExecution(rs.getTimestamp("next_execution").toLocalDateTime());
            }
            
            if (rs.getTimestamp("created_at") != null) {
                query.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            
            query.setClosed(rs.getBoolean("closed"));
            
            // User information
            query.setUserEmail(rs.getString("email"));
            query.setDiscordWebhook(rs.getString("discord_webhook"));
            query.setSlackWebhook(rs.getString("slack_webhook"));
            query.setWhatsappPhone(rs.getString("whatsapp_phone"));
            
            return query;
        }
    }
}