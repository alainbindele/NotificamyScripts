package com.notifyme.infrastructure.adapter.inbound.web;

import com.notifyme.domain.port.inbound.NotificationPollerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for manual notification poller operations
 */
@Slf4j
@RestController
@RequestMapping("/api/poller")
@RequiredArgsConstructor
public class NotificationPollerController {
    
    private final NotificationPollerUseCase notificationPollerUseCase;
    
    /**
     * Manually trigger the notification poller job
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerPoller() {
        try {
            Long executionId = notificationPollerUseCase.triggerPollerJob();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification poller job triggered successfully",
                "executionId", executionId
            ));
            
        } catch (NotificationPollerUseCase.NotificationPollerException e) {
            log.error("Failed to trigger notification poller job", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to trigger notification poller job",
                "error", e.getMessage()
            ));
        }
    }
}