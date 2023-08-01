package com.zilch.payments.cards.mappers;

import com.zilch.payments.cards.models.Card;
import com.zilch.payments.models.AddCardRequest;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@DecoratedWith(CardMapperDecorator.class)
public interface CardMapper {

  @Mapping(target = "creationDateTime", expression = "java(java.time.OffsetDateTime.now())")
  @Mapping(target = "validFrom", ignore = true)
  @Mapping(target = "validUpto", ignore = true)
  Card addCardRequestToCard(AddCardRequest addCardRequest);
}
