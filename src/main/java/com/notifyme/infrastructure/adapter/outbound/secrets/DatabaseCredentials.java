package com.notifyme.infrastructure.adapter.outbound.secrets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Database credentials retrieved from AWS Secrets Manager
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseCredentials {
    
    private String url;
    private String username;
    private String password;
    private String sqsQueueUrl;
    
    public boolean isValid() {
        return url != null && !url.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }
}