package com.notifyme.infrastructure.adapter.outbound.secrets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsSecretsManagerServiceTest {

    @Mock
    private SecretsManagerClient secretsManagerClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AwsSecretsManagerService secretsManagerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(secretsManagerService, "databaseCredentialsSecretName", 
                "notificamy/database-credentials");
        ReflectionTestUtils.setField(secretsManagerService, "objectMapper", new ObjectMapper());
    }

    @Test
    void testGetDatabaseCredentials_Success() {
        // Given
        String secretJson = """
            {
                "DB_URL": "jdbc:mysql://test-host:3306/testdb",
                "DB_USER": "testuser",
                "DB_PASSWORD": "testpass",
                "AWS_SQS_QUEUE_URL": "https://sqs.eu-south-1.amazonaws.com/123456789/test-queue.fifo"
            }
            """;

        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(secretJson)
                .build();

        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        // When
        DatabaseCredentials credentials = secretsManagerService.getDatabaseCredentials();

        // Then
        assertNotNull(credentials);
        assertEquals("jdbc:mysql://test-host:3306/testdb", credentials.getUrl());
        assertEquals("testuser", credentials.getUsername());
        assertEquals("testpass", credentials.getPassword());
        assertEquals("https://sqs.eu-south-1.amazonaws.com/123456789/test-queue.fifo", 
                credentials.getSqsQueueUrl());
        assertTrue(credentials.isValid());
    }

    @Test
    void testGetDatabaseCredentials_Exception() {
        // Given
        when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(new RuntimeException("AWS error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            secretsManagerService.getDatabaseCredentials();
        });
    }
}