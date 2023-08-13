package com.zilch.payments.messaging.events.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Slf4j
public final class ZilchKafkaDeserializer<T> implements Deserializer<T> {

  private final JsonDeserializer<T> jsonDeserializer;

  public ZilchKafkaDeserializer(Class<T> classType) {
    ObjectMapper objectMapper =
        new ObjectMapper()
            .registerModules(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonDeserializer = new JsonDeserializer<>(classType, objectMapper);
  }

  public void configure(Map<String, ?> configs, boolean isKey) {}

  public T deserialize(String topic, byte[] data) {
    try {
      return jsonDeserializer.deserialize(topic, data);
    } catch (Exception exe) {
      log.error(" Deserialization Exception ::", exe);
    }
    return null;
  }

  public void close() {}
}
