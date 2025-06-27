package com.notifyme.infrastructure.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity for users
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "discord_webhook")
    private String discordWebhook;
    
    @Column(name = "slack_webhook")
    private String slackWebhook;
    
    @Column(name = "whatsapp_phone")
    private String whatsappPhone;
}