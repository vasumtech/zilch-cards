package com.zilch.payments.messaging.events.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MessageEventsProducer {

  private final MessageEventsProducerConfig messageEventsProducerConfig;

  public void send(String topicName, Object object) {
    messageEventsProducerConfig.send(topicName, object);
  }
}
