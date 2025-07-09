package com.notifyme.infrastructure.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for notification queries
 */
@Entity
@Table(name = "queries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;
    
    @Column(name = "cron_params")
    private String cronParams;
    
    @Column(name = "next_execution")
    private LocalDateTime nextExecution;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "is_valid")
    private Boolean isValid;
    
    @Column(name = "closed")
    private Boolean closed;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to")
    private LocalDateTime validTo;
}