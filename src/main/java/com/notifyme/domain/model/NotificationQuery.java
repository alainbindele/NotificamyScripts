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
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    
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
    
    /**
     * Check if the query is within its validity period at the given time
     * 
     * @param checkTime The time to check against
     * @return true if the query is valid at the given time
     */
    public boolean isWithinValidityPeriod(LocalDateTime checkTime) {
        // If no validity constraints are set, it's always valid
        if (validFrom == null && validTo == null) {
            return true;
        }
        
        // Check valid_from constraint
        if (validFrom != null && checkTime.isBefore(validFrom)) {
            return false;
        }
        
        // Check valid_to constraint
        if (validTo != null && checkTime.isAfter(validTo)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the query is currently within its validity period
     * 
     * @return true if the query is valid now
     */
    public boolean isCurrentlyValid() {
        return isWithinValidityPeriod(LocalDateTime.now());
    }
}