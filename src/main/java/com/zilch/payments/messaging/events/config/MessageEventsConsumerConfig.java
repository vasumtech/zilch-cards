package com.zilch.payments.messaging.events.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class MessageEventsConsumerConfig {

  @Value(value = "${spring.kafka.bootstrap-servers:#{null}}")
  private String bootstrapAddress;

  public <T> void register(
      String topicName,
      Class<T> messageType,
      String groupId,
      ZilchMessageEventsListener listenerObject) {
    if (bootstrapAddress != null) {
      ContainerProperties containerProperties = new ContainerProperties(topicName);
      containerProperties.setMessageListener(listenerObject);
      final DefaultKafkaConsumerFactory<String, T> defaultKafkaConsumerFactory =
          new DefaultKafkaConsumerFactory<>(
              getKafkaConsumerProperties(groupId),
              new StringDeserializer(),
              new ZilchKafkaDeserializer<>(messageType));
      final ConcurrentMessageListenerContainer container =
          new ConcurrentMessageListenerContainer<>(
              defaultKafkaConsumerFactory, containerProperties);
      container.start();
    } else {
      log.warn(" Message Broker NOT configured !! ");
    }
  }

  private Map<String, Object> getKafkaConsumerProperties(String groupId) {
    Map<String, Object> consumerProperties = new HashMap<>();
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    return consumerProperties;
  }
}
