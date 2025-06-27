package com.notifyme.infrastructure.adapter.outbound.persistence.mapper;

import com.notifyme.domain.model.NotificationQuery;
import com.notifyme.infrastructure.adapter.outbound.persistence.entity.NotificationQueryEntity;
import com.notifyme.infrastructure.adapter.outbound.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper for NotificationQuery domain model and JPA entities
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationQueryMapper {
    
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "prompt", source = "entity.prompt")
    @Mapping(target = "cronParams", source = "entity.cronParams")
    @Mapping(target = "nextExecution", source = "entity.nextExecution")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "userId", source = "entity.userId")
    @Mapping(target = "isValid", source = "entity.isValid")
    @Mapping(target = "closed", source = "entity.closed")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "discordWebhook", source = "user.discordWebhook")
    @Mapping(target = "slackWebhook", source = "user.slackWebhook")
    @Mapping(target = "whatsappPhone", source = "user.whatsappPhone")
    NotificationQuery toDomain(NotificationQueryEntity entity, UserEntity user);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "prompt", source = "prompt")
    @Mapping(target = "cronParams", source = "cronParams")
    @Mapping(target = "nextExecution", source = "nextExecution")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "isValid", source = "isValid")
    @Mapping(target = "closed", source = "closed")
    NotificationQueryEntity toEntity(NotificationQuery domain);
}