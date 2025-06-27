package com.notifyme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for notification query
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQuery {
    
    private Long id;
    private String prompt;
    private String cronParams;
    private LocalDateTime nextExecution;
    private LocalDateTime createdAt;
    private Long userId;
    private Boolean isValid;
    private Boolean closed;
    
    // User information
    private String userEmail;
    private String discordWebhook;
    private String slackWebhook;
    private String whatsappPhone;
    
    public boolean isClosed() {
        return Boolean.TRUE.equals(closed);
    }
    
    public boolean isValid() {
        return Boolean.TRUE.equals(isValid);
    }
    
    public boolean hasValidPrompt() {
        return prompt != null && !prompt.trim().isEmpty();
    }
    
    public boolean hasValidEmail() {
        return userEmail != null && !userEmail.trim().isEmpty();
    }
    
    public boolean hasCronParams() {
        return cronParams != null && !cronParams.trim().isEmpty() && !"NULL".equals(cronParams);
    }
}