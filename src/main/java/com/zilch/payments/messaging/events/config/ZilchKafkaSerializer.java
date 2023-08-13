package com.zilch.payments.messaging.events.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Slf4j
public class ZilchKafkaSerializer<T> implements Serializer<T> {

  private final JsonSerializer<T> jsonSerializer;

  public ZilchKafkaSerializer() {
    ObjectMapper objectMapper =
        new ObjectMapper().registerModules(new JavaTimeModule()).registerModule(new Jdk8Module());
    this.jsonSerializer = new JsonSerializer<>(objectMapper);
  }

  @Override
  public void configure(Map<String, ?> map, boolean b) {}

  @Override
  public byte[] serialize(String topic, T data) {
    try {
      return jsonSerializer.serialize(topic, data);
    } catch (Exception exe) {
      log.error(" Serialization exception ::", exe);
    }
    return null;
  }

  @Override
  public void close() {}
}
