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
    
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "discordWebhook", source = "user.discordWebhook")
    @Mapping(target = "slackWebhook", source = "user.slackWebhook")
    @Mapping(target = "whatsappPhone", source = "user.whatsappPhone")
    NotificationQuery toDomain(NotificationQueryEntity entity, UserEntity user);
    
    NotificationQueryEntity toEntity(NotificationQuery domain);
}