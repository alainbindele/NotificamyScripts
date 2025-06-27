package com.notifyme.batch.writer;

import com.notifyme.model.SQSNotificationMessage;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Composite writer that sends to SQS and updates database
 */
@Configuration
public class CompositeNotificationWriter {

    @Autowired
    private SQSNotificationWriter sqsNotificationWriter;

    @Bean
    public ItemWriter<SQSNotificationMessage> compositeNotificationWriter() {
        CompositeItemWriter<SQSNotificationMessage> writer = new CompositeItemWriter<>();
        writer.setDelegates(Arrays.asList(sqsNotificationWriter));
        return writer;
    }
}