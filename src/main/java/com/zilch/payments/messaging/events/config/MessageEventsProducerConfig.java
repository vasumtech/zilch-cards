package com.zilch.payments.messaging.events.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Configuration
@SuppressWarnings("rawtypes")
public class MessageEventsProducerConfig {

  private static final String KAFKA_EVENT_TOPICS_LIST = "zilch-kafka-event-topics.txt";

  @Value(value = "${spring.kafka.bootstrap-servers:#{null}}")
  private String kafkaServer;

  private static final Map<String, KafkaTemplate> kafkaTemplates = new HashMap<>();

  @PostConstruct
  public <T> void init() {
    if (kafkaServer != null) {
      List<String> eventTopics = getEventTopics();
      eventTopics.forEach(this::accept);
    } else {
      log.warn(" Message Broker NOT configured !! ");
    }
  }

  protected void send(String topicName, Object object) {
    KafkaTemplate kafkaTemplate = kafkaTemplates.get(topicName);
    if (kafkaTemplate != null) {
      kafkaTemplate.send(topicName, object);
    } else {
      throw new IllegalStateException(
          "Can't find event type to publish event for topic=" + topicName);
    }
  }

  private List<String> getEventTopics() {
    try (InputStream inputStream =
        this.getClass().getClassLoader().getResourceAsStream(KAFKA_EVENT_TOPICS_LIST)) {
      assert inputStream != null;
      try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
          BufferedReader bufferedReader = new BufferedReader(inputStreamReader); ) {
        return bufferedReader.lines().toList();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> void accept(String topic) {
    final DefaultKafkaProducerFactory<String, T> producerFactory =
        new DefaultKafkaProducerFactory<>(
            Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer),
            new StringSerializer(),
            new ZilchKafkaSerializer<>());
    kafkaTemplates.put(topic, new KafkaTemplate<>(producerFactory));
  }

}
