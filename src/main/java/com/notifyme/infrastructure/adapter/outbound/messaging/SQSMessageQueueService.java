package com.notifyme.infrastructure.adapter.outbound.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.domain.port.outbound.MessageQueueService;
import com.notifyme.infrastructure.adapter.outbound.messaging.dto.SQSNotificationMessageDto;
import com.notifyme.infrastructure.adapter.outbound.messaging.mapper.SQSMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

/**
 * SQS implementation of MessageQueueService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SQSMessageQueueService implements MessageQueueService {
    
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final SQSMessageMapper messageMapper;
    
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;
    
    @Value("${aws.sqs.batch-size}")
    private int batchSize;
    
    @Override
    public void sendMessages(List<NotificationMessage> messages) throws MessageQueueException {
        if (messages.isEmpty()) {
            return;
        }
        
        log.info("Sending {} messages to SQS queue", messages.size());
        
        try {
            // Split into batches (SQS supports max 10 messages per batch)
            List<List<NotificationMessage>> batches = Lists.partition(messages, batchSize);
            
            for (List<NotificationMessage> batch : batches) {
                sendBatchToSQS(batch);
            }
            
            log.info("Successfully sent {} messages to SQS in {} batches", 
                    messages.size(), batches.size());
                    
        } catch (Exception e) {
            log.error("Failed to send messages to SQS", e);
            throw new MessageQueueException("Failed to send messages to SQS", e);
        }
    }
    
    @Override
    public void sendMessage(NotificationMessage message) throws MessageQueueException {
        sendMessages(List.of(message));
    }
    
    private void sendBatchToSQS(List<NotificationMessage> messages) throws Exception {
        List<SendMessageBatchRequestEntry> entries = IntStream.range(0, messages.size())
            .mapToObj(i -> {
                NotificationMessage message = messages.get(i);
                try {
                    SQSNotificationMessageDto dto = messageMapper.toDto(message);
                    String messageBody = objectMapper.writeValueAsString(dto);
                    
                    return SendMessageBatchRequestEntry.builder()
                        .id(String.valueOf(i))
                        .messageBody(messageBody)
                        .messageGroupId(String.valueOf(Instant.now().getEpochSecond()))
                        .build();
                        
                } catch (Exception e) {
                    log.error("Failed to serialize message for query ID: {}", 
                             message.getQueryId(), e);
                    throw new RuntimeException("Failed to serialize SQS message", e);
                }
            })
            .toList();
        
        SendMessageBatchRequest batchRequest = SendMessageBatchRequest.builder()
            .queueUrl(queueUrl)
            .entries(entries)
            .build();
        
        SendMessageBatchResponse response = sqsClient.sendMessageBatch(batchRequest);
        
        if (!response.failed().isEmpty()) {
            log.error("Failed to send {} messages to SQS: {}", 
                     response.failed().size(), response.failed());
            throw new RuntimeException("Some messages failed to send to SQS");
        }
        
        log.debug("Successfully sent batch of {} messages to SQS", entries.size());
    }
}