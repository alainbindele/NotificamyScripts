package com.notifyme.domain.port.outbound;

import com.notifyme.domain.model.NotificationMessage;

import java.util.List;

/**
 * Port for message queue operations
 */
public interface MessageQueueService {
    
    /**
     * Send messages to the queue in batch
     * 
     * @param messages List of messages to send
     * @throws MessageQueueException if sending fails
     */
    void sendMessages(List<NotificationMessage> messages) throws MessageQueueException;
    
    /**
     * Send a single message to the queue
     * 
     * @param message Message to send
     * @throws MessageQueueException if sending fails
     */
    void sendMessage(NotificationMessage message) throws MessageQueueException;
    
    /**
     * Exception thrown when message queue operations fail
     */
    class MessageQueueException extends Exception {
        public MessageQueueException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public MessageQueueException(String message) {
            super(message);
        }
    }
}