package com.zilch.payments.cards.events.consumers;

import com.zilch.payments.cards.events.dto.NewCardEvent;
import com.zilch.payments.messaging.events.config.MessageEventsConsumerConfig;
import com.zilch.payments.messaging.events.config.ZilchMessageEventsListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class NewCardEventConsumer implements ZilchMessageEventsListener {

  private static final String NEW_CARD_EVENT_GROUP_ID = "newcard-event-group-id";

  private static final String NEW_CARD_EVENT_TOPIC_NAME = "com.zilch.payments.cards.newcard";

  private final MessageEventsConsumerConfig messageEventsConsumerConfig;

  @PostConstruct
  public void init() {
    messageEventsConsumerConfig.register(
        NEW_CARD_EVENT_TOPIC_NAME, NewCardEvent.class, NEW_CARD_EVENT_GROUP_ID, this);
  }

  @Override
  public void onMessage(@NonNull Object message) {
    NewCardEvent newCardEvent = (NewCardEvent) getMessage(message);
    //IMPORTANT: We need to discuss and add the business functionality after consuming the event!!
    // For example, we can call a third party service and update their systems etc.
    log.info(" card event consumed : {}", newCardEvent);
  }
}
