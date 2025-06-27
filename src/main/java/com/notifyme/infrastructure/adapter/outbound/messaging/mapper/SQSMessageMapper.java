package com.notifyme.infrastructure.adapter.outbound.messaging.mapper;

import com.notifyme.domain.model.NotificationMessage;
import com.notifyme.infrastructure.adapter.outbound.messaging.dto.SQSNotificationMessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for NotificationMessage domain model and SQS DTO
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SQSMessageMapper {
    
    SQSNotificationMessageDto toDto(NotificationMessage domain);
    
    NotificationMessage toDomain(SQSNotificationMessageDto dto);
}