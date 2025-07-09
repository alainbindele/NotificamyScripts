package com.notifyme.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuration for application timezone handling
 */
@Slf4j
@Configuration
public class TimezoneConfig {
    
    @Value("${app.timezone:UTC}")
    private String applicationTimezone;
    
    @PostConstruct
    public void init() {
        // Set JVM default timezone
        TimeZone.setDefault(TimeZone.getTimeZone(applicationTimezone));
        
        log.info("Application timezone set to: {}", applicationTimezone);
        log.info("JVM default timezone: {}", TimeZone.getDefault().getID());
        log.info("System default ZoneId: {}", ZoneId.systemDefault());
    }
    
    public ZoneId getApplicationZoneId() {
        return ZoneId.of(applicationTimezone);
    }
}