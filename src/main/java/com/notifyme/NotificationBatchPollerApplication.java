package com.notifyme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for NotifyMe Batch Poller
 * 
 * @author Alain Kiesse Bindele
 * @email alain.bindele@gmail.com
 */
@SpringBootApplication
@EnableScheduling
public class NotificationBatchPollerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationBatchPollerApplication.class, args);
    }
}