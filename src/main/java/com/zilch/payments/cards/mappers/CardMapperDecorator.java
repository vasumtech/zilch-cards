package com.zilch.payments.cards.mappers;

import com.zilch.payments.cards.components.CardInputDatesConverter;
import com.zilch.payments.cards.models.Card;
import com.zilch.payments.models.AddCardRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class CardMapperDecorator implements CardMapper {

  @Autowired private CardMapper cardMapper;

  @Autowired private CardInputDatesConverter cardInputDatesConverter;

  @Override
  public Card addCardRequestToCard(AddCardRequest addCardRequest) {
    Card card = cardMapper.addCardRequestToCard(addCardRequest);
    card.setValidFrom(cardInputDatesConverter.parseValidFromDate(addCardRequest.getValidFrom()));
    card.setValidUpto(cardInputDatesConverter.parseValidUntilDate(addCardRequest.getValidUntil()));
    return card;
  }
}
