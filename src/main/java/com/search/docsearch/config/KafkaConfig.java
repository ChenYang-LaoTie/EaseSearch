package com.search.docsearch.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${kafka.need}")
    private boolean needKafka;

    @Value("${kafka.bootstrap}")
    private String bootstrap;

    @Value("${kafka.consumer.group}")

    @Bean()
    public KafkaProducer<String, String> kafkaProducerClient() {
        if (needKafka) {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrap);

            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());

            KafkaProducer<String, String> producer = new KafkaProducer(props);
            return producer;
        }
        return null;
    }

    @Bean()
    public KafkaConsumer<String, String> kafkaConsumerClient() {
        if (needKafka) {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrap);

            props.put("group.id", "openeuler");
            props.put("key.deserializer", StringDeserializer.class);
            props.put("value.deserializer", StringDeserializer.class);


            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            return consumer;
        }
        return null;
    }




}
