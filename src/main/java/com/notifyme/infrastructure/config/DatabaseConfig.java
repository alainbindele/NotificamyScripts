package com.notifyme.infrastructure.config;

import com.notifyme.infrastructure.adapter.outbound.secrets.AwsSecretsManagerService;
import com.notifyme.infrastructure.adapter.outbound.secrets.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database configuration with AWS Secrets Manager integration
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
    
    private final AwsSecretsManagerService secretsManagerService;
    
    @Value("${aws.secrets.enabled:true}")
    private boolean secretsEnabled;
    
    // Fallback values for local development
    @Value("${spring.datasource.url:}")
    private String fallbackJdbcUrl;
    
    @Value("${spring.datasource.username:}")
    private String fallbackUsername;
    
    @Value("${spring.datasource.password:}")
    private String fallbackPassword;
    
    @Bean
    @Primary
    public DataSource dataSource() {
        DatabaseCredentials credentials = getDatabaseCredentials();
        
        log.info("Configuring database connection to: {}", 
                maskUrl(credentials.getUrl()));
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(credentials.getUrl());
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // Connection pool optimization for batch processing
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }
    
    /**
     * Get database credentials from AWS Secrets Manager or fallback to environment variables
     */
    private DatabaseCredentials getDatabaseCredentials() {
        if (secretsEnabled) {
            try {
                log.info("Retrieving database credentials from AWS Secrets Manager");
                DatabaseCredentials credentials = secretsManagerService.getDatabaseCredentials();
                
                if (credentials.isValid()) {
                    log.info("Successfully retrieved database credentials from AWS Secrets Manager");
                    return credentials;
                } else {
                    log.warn("Invalid credentials from AWS Secrets Manager, falling back to environment variables");
                }
            } catch (Exception e) {
                log.error("Failed to retrieve credentials from AWS Secrets Manager, falling back to environment variables", e);
            }
        }
        
        // Fallback to environment variables or application properties
        log.info("Using fallback database credentials from environment variables");
        return DatabaseCredentials.builder()
            .url(fallbackJdbcUrl)
            .username(fallbackUsername)
            .password(fallbackPassword)
            .build();
    }
    
    /**
     * Mask sensitive parts of the database URL for logging
     */
    private String maskUrl(String url) {
        if (url == null) return "null";
        
        // Mask password in URL if present
        return url.replaceAll("password=[^&]*", "password=***");
    }
}