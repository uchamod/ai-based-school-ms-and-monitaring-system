package com.example.sclms_auth.Config;

import com.example.sclms_auth.DTO.NotificationEvent;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
//import com.fasterxml.jackson.databind.JsonSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Step 1: Create producer settings
    @Bean
    public ProducerFactory<String, NotificationEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka server address
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // How to convert message key to bytes
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        // How to convert NotificationEvent object to bytes
        /*
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class.getName());
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class.getName());
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);*/
        return new DefaultKafkaProducerFactory<>(config);
    }

    // Step 2: Create the KafkaTemplate bean using those settings
    @Bean
    public KafkaTemplate<String, NotificationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
