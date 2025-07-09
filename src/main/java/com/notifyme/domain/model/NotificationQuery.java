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
    private String timezone; // New field for per-query timezone
    
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
     * Get the timezone for this query, defaulting to UTC if not specified
     * 
     * @return ZoneId for this query
     */
    public java.time.ZoneId getQueryZoneId() {
        if (timezone != null && !timezone.trim().isEmpty() && !"NULL".equals(timezone)) {
            try {
                return java.time.ZoneId.of(timezone);
            } catch (Exception e) {
                // Log warning and fallback to UTC
                return java.time.ZoneId.of("UTC");
            }
        }
        return java.time.ZoneId.of("UTC"); // Default to UTC
    }
    
    /**
     * Check if this query has a specific timezone configured
     * 
     * @return true if timezone is configured
     */
    public boolean hasTimezone() {
        return timezone != null && !timezone.trim().isEmpty() && !"NULL".equals(timezone);
    }
    
    /**
     * Check if the query is within its validity period at the given time
     * This method assumes the checkTime is in UTC (as stored in database)
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
     * This uses current UTC time for consistency with database
     * 
     * @return true if the query is valid now
     */
    public boolean isCurrentlyValid() {
        // Use UTC time for consistency with database storage
        return isWithinValidityPeriod(java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")));
    }
}