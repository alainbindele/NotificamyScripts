package com.notifyme.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Message structure for SQS notifications
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SQSNotificationMessage {

    @NotNull
    @JsonProperty("query_id")
    private Long queryId;

    @NotBlank
    @JsonProperty("user_email")
    private String userEmail;

    @NotBlank
    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("user_discord_webhook")
    private String userDiscordWebhook;

    @JsonProperty("user_slack_webhook")
    private String userSlackWebhook;

    @JsonProperty("user_phone")
    private String userPhone;

    // Constructors
    public SQSNotificationMessage() {}

    public SQSNotificationMessage(Long queryId, String userEmail, String prompt) {
        this.queryId = queryId;
        this.userEmail = userEmail;
        this.prompt = prompt;
    }

    // Builder pattern for optional fields
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SQSNotificationMessage message = new SQSNotificationMessage();

        public Builder queryId(Long queryId) {
            message.queryId = queryId;
            return this;
        }

        public Builder userEmail(String userEmail) {
            message.userEmail = userEmail;
            return this;
        }

        public Builder prompt(String prompt) {
            message.prompt = prompt;
            return this;
        }

        public Builder userDiscordWebhook(String webhook) {
            if (webhook != null && !webhook.trim().isEmpty() && !"NULL".equals(webhook)) {
                message.userDiscordWebhook = webhook;
            }
            return this;
        }

        public Builder userSlackWebhook(String webhook) {
            if (webhook != null && !webhook.trim().isEmpty() && !"NULL".equals(webhook)) {
                message.userSlackWebhook = webhook;
            }
            return this;
        }

        public Builder userPhone(String phone) {
            if (phone != null && !phone.trim().isEmpty() && !"NULL".equals(phone)) {
                message.userPhone = phone;
            }
            return this;
        }

        public SQSNotificationMessage build() {
            return message;
        }
    }

    // Getters and Setters
    public Long getQueryId() { return queryId; }
    public void setQueryId(Long queryId) { this.queryId = queryId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getUserDiscordWebhook() { return userDiscordWebhook; }
    public void setUserDiscordWebhook(String userDiscordWebhook) { this.userDiscordWebhook = userDiscordWebhook; }

    public String getUserSlackWebhook() { return userSlackWebhook; }
    public void setUserSlackWebhook(String userSlackWebhook) { this.userSlackWebhook = userSlackWebhook; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    @Override
    public String toString() {
        return "SQSNotificationMessage{" +
                "queryId=" + queryId +
                ", userEmail='" + userEmail + '\'' +
                ", prompt='" + prompt + '\'' +
                ", hasDiscordWebhook=" + (userDiscordWebhook != null) +
                ", hasSlackWebhook=" + (userSlackWebhook != null) +
                ", hasPhone=" + (userPhone != null) +
                '}';
    }
}