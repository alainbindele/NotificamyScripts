package com.notifyme.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing a notification query
 */
@Entity
@Table(name = "queries")
public class NotificationQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "cron_params")
    private String cronParams;

    @Column(name = "next_execution")
    private LocalDateTime nextExecution;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "is_valid")
    private Boolean isValid = true;

    @Column(name = "closed")
    private Boolean closed = false;

    // User information (joined from users table)
    @Transient
    private String userEmail;

    @Transient
    private String discordWebhook;

    @Transient
    private String slackWebhook;

    @Transient
    private String whatsappPhone;

    // Constructors
    public NotificationQuery() {}

    public NotificationQuery(Long id, String prompt, String cronParams, 
                           LocalDateTime nextExecution, LocalDateTime createdAt,
                           String userEmail, String discordWebhook, 
                           String slackWebhook, String whatsappPhone, Boolean closed) {
        this.id = id;
        this.prompt = prompt;
        this.cronParams = cronParams;
        this.nextExecution = nextExecution;
        this.createdAt = createdAt;
        this.userEmail = userEmail;
        this.discordWebhook = discordWebhook;
        this.slackWebhook = slackWebhook;
        this.whatsappPhone = whatsappPhone;
        this.closed = closed;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getCronParams() { return cronParams; }
    public void setCronParams(String cronParams) { this.cronParams = cronParams; }

    public LocalDateTime getNextExecution() { return nextExecution; }
    public void setNextExecution(LocalDateTime nextExecution) { this.nextExecution = nextExecution; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }

    public Boolean getClosed() { return closed; }
    public void setClosed(Boolean closed) { this.closed = closed; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getDiscordWebhook() { return discordWebhook; }
    public void setDiscordWebhook(String discordWebhook) { this.discordWebhook = discordWebhook; }

    public String getSlackWebhook() { return slackWebhook; }
    public void setSlackWebhook(String slackWebhook) { this.slackWebhook = slackWebhook; }

    public String getWhatsappPhone() { return whatsappPhone; }
    public void setWhatsappPhone(String whatsappPhone) { this.whatsappPhone = whatsappPhone; }

    @Override
    public String toString() {
        return "NotificationQuery{" +
                "id=" + id +
                ", prompt='" + prompt + '\'' +
                ", cronParams='" + cronParams + '\'' +
                ", nextExecution=" + nextExecution +
                ", userEmail='" + userEmail + '\'' +
                ", closed=" + closed +
                '}';
    }
}