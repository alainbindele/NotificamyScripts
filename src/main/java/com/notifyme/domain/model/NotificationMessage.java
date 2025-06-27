package com.notifyme.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for notification message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    private Long queryId;
    private String userEmail;
    private String prompt;
    private String userDiscordWebhook;
    private String userSlackWebhook;
    private String userPhone;
    
    public static NotificationMessage fromQuery(NotificationQuery query) {
        return NotificationMessage.builder()
            .queryId(query.getId())
            .userEmail(query.getUserEmail())
            .prompt(query.getPrompt())
            .userDiscordWebhook(sanitizeWebhook(query.getDiscordWebhook()))
            .userSlackWebhook(sanitizeWebhook(query.getSlackWebhook()))
            .userPhone(sanitizePhone(query.getWhatsappPhone()))
            .build();
    }
    
    private static String sanitizeWebhook(String webhook) {
        return (webhook != null && !webhook.trim().isEmpty() && !"NULL".equals(webhook)) 
            ? webhook : null;
    }
    
    private static String sanitizePhone(String phone) {
        return (phone != null && !phone.trim().isEmpty() && !"NULL".equals(phone)) 
            ? phone : null;
    }
}