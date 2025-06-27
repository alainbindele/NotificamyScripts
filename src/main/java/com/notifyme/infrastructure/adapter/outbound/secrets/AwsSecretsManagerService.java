package com.notifyme.infrastructure.adapter.outbound.secrets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for retrieving secrets from AWS Secrets Manager
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwsSecretsManagerService {
    
    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.secrets.database-credentials-name:notificamy/database-credentials}")
    private String databaseCredentialsSecretName;
    
    private Map<String, String> secretsCache = new HashMap<>();
    
    /**
     * Get database credentials from AWS Secrets Manager
     */
    public DatabaseCredentials getDatabaseCredentials() {
        log.info("Retrieving database credentials from AWS Secrets Manager: {}", databaseCredentialsSecretName);
        
        try {
            Map<String, String> secrets = getSecrets(databaseCredentialsSecretName);
            
            return DatabaseCredentials.builder()
                .url(secrets.get("DB_URL"))
                .username(secrets.get("DB_USER"))
                .password(secrets.get("DB_PASSWORD"))
                .sqsQueueUrl(secrets.get("AWS_SQS_QUEUE_URL"))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve database credentials from AWS Secrets Manager", e);
            throw new RuntimeException("Failed to retrieve database credentials", e);
        }
    }
    
    /**
     * Get secrets from AWS Secrets Manager with caching
     */
    private Map<String, String> getSecrets(String secretName) {
        // Check cache first
        String cacheKey = secretName;
        if (secretsCache.containsKey(cacheKey)) {
            log.debug("Using cached secrets for: {}", secretName);
            return parseSecretString(secretsCache.get(cacheKey));
        }
        
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
            
            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();
            
            // Cache the secret
            secretsCache.put(cacheKey, secretString);
            
            log.info("Successfully retrieved secrets from AWS Secrets Manager: {}", secretName);
            return parseSecretString(secretString);
            
        } catch (Exception e) {
            log.error("Failed to retrieve secret: {}", secretName, e);
            throw new RuntimeException("Failed to retrieve secret: " + secretName, e);
        }
    }
    
    /**
     * Parse secret string (JSON format) into a map
     */
    private Map<String, String> parseSecretString(String secretString) {
        try {
            Map<String, String> secrets = new HashMap<>();
            JsonNode jsonNode = objectMapper.readTree(secretString);
            
            jsonNode.fields().forEachRemaining(entry -> 
                secrets.put(entry.getKey(), entry.getValue().asText())
            );
            
            return secrets;
            
        } catch (Exception e) {
            log.error("Failed to parse secret string", e);
            throw new RuntimeException("Failed to parse secret string", e);
        }
    }
    
    /**
     * Clear secrets cache (useful for testing or credential rotation)
     */
    public void clearCache() {
        secretsCache.clear();
        log.info("Secrets cache cleared");
    }
}