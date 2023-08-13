package com.zilch.payments.messaging.events.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

@SuppressWarnings("rawtypes")
public interface ZilchMessageEventsListener extends MessageListener {

  default Object getMessage(Object message) {
    ConsumerRecord consumerRecord = (ConsumerRecord) message;
    return consumerRecord.value();
  }
}
