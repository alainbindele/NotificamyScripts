package com.notifyme.controller;

import com.notifyme.service.NotificationPollerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for manual notification poller operations
 */
@RestController
@RequestMapping("/api/poller")
public class NotificationPollerController {

    @Autowired
    private NotificationPollerService notificationPollerService;

    /**
     * Manually trigger the notification poller job
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerPoller() {
        try {
            Long executionId = notificationPollerService.triggerPollerJob();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification poller job triggered successfully",
                "executionId", executionId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to trigger notification poller job",
                "error", e.getMessage()
            ));
        }
    }
}