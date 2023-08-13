package com.zilch.payments.cards.events.producers;

import com.zilch.payments.cards.events.dto.NewCardEvent;
import com.zilch.payments.messaging.events.config.MessageEventsProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class NewCardEventPublisher {

  private static final String NEW_CARD_EVENT_TOPIC_NAME = "com.zilch.payments.cards.newcard";

  private final MessageEventsProducer messageEventsProducer;

  public void publishNewCardEvent(NewCardEvent newCardEvent) {
    messageEventsProducer.send(NEW_CARD_EVENT_TOPIC_NAME, newCardEvent);
    log.info(" card event published : {}", newCardEvent);
  }
}
