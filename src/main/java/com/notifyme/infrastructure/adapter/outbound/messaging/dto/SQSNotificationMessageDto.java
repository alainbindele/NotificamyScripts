package com.notifyme.infrastructure.adapter.outbound.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SQS notification messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SQSNotificationMessageDto {
    
    @JsonProperty("query_id")
    private Long queryId;
    
    @JsonProperty("user_email")
    private String userEmail;
    
    @JsonProperty("prompt")
    private String prompt;
    
    @JsonProperty("user_discord_webhook")
    private String userDiscordWebhook;
    
    @JsonProperty("user_slack_webhook")
    private String userSlackWebhook;
    
    @JsonProperty("user_phone")
    private String userPhone;
}