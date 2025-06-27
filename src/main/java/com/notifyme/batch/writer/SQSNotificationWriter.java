package com.notifyme.batch.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.notifyme.model.SQSNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Writer that sends notification messages to AWS SQS in batches
 */
@Component
public class SQSNotificationWriter implements ItemWriter<SQSNotificationMessage> {

    private static final Logger logger = LoggerFactory.getLogger(SQSNotificationWriter.class);

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.batch-size}")
    private int batchSize;

    @Override
    public void write(Chunk<? extends SQSNotificationMessage> chunk) throws Exception {
        List<? extends SQSNotificationMessage> messages = chunk.getItems();
        
        if (messages.isEmpty()) {
            return;
        }

        logger.info("Writing {} messages to SQS queue", messages.size());

        // Split into batches (SQS supports max 10 messages per batch)
        List<List<? extends SQSNotificationMessage>> batches = Lists.partition(messages, batchSize);

        for (List<? extends SQSNotificationMessage> batch : batches) {
            sendBatchToSQS(batch);
        }

        logger.info("Successfully sent {} messages to SQS in {} batches", 
                   messages.size(), batches.size());
    }

    private void sendBatchToSQS(List<? extends SQSNotificationMessage> messages) throws Exception {
        List<SendMessageBatchRequestEntry> entries = IntStream.range(0, messages.size())
            .mapToObj(i -> {
                SQSNotificationMessage message = messages.get(i);
                try {
                    String messageBody = objectMapper.writeValueAsString(message);
                    
                    return SendMessageBatchRequestEntry.builder()
                        .id(String.valueOf(i))
                        .messageBody(messageBody)
                        .messageGroupId(String.valueOf(Instant.now().getEpochSecond()))
                        .build();
                        
                } catch (Exception e) {
                    logger.error("Failed to serialize message for query ID: {}", 
                               message.getQueryId(), e);
                    throw new RuntimeException("Failed to serialize SQS message", e);
                }
            })
            .toList();

        SendMessageBatchRequest batchRequest = SendMessageBatchRequest.builder()
            .queueUrl(queueUrl)
            .entries(entries)
            .build();

        try {
            SendMessageBatchResponse response = sqsClient.sendMessageBatch(batchRequest);
            
            if (!response.failed().isEmpty()) {
                logger.error("Failed to send {} messages to SQS: {}", 
                           response.failed().size(), response.failed());
                throw new RuntimeException("Some messages failed to send to SQS");
            }
            
            logger.debug("Successfully sent batch of {} messages to SQS", entries.size());
            
        } catch (Exception e) {
            logger.error("Failed to send batch to SQS", e);
            throw new RuntimeException("Failed to send messages to SQS", e);
        }
    }
}