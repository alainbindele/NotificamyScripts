package com.notifyme.domain.port.inbound;

/**
 * Use case for notification polling operations
 */
public interface NotificationPollerUseCase {
    
    /**
     * Trigger the notification poller job manually
     * 
     * @return Job execution ID
     * @throws NotificationPollerException if job fails to start
     */
    Long triggerPollerJob() throws NotificationPollerException;
    
    /**
     * Exception thrown when poller operations fail
     */
    class NotificationPollerException extends Exception {
        public NotificationPollerException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public NotificationPollerException(String message) {
            super(message);
        }
    }
}