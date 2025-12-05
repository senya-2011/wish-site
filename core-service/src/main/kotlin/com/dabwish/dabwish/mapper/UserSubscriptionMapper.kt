package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.generated.dto.SubscriptionResponse
import com.dabwish.dabwish.model.user.UserSubscription
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface UserSubscriptionMapper {
    @Mapping(source = "subscriber.id", target = "subscriberId")
    @Mapping(source = "subscribedTo.id", target = "subscribedToId")
    @Mapping(source = "createdAt", target = "createdAt")
    fun toResponse(subscription: UserSubscription): SubscriptionResponse
}

