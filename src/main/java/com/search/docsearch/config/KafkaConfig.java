package com.search.docsearch.config;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Value("${kafka.need}")
    private boolean needKafka;

    @Value("${kafka.bootstrap}")
    private String bootstrap;

    @Value("${kafka.consumer.group}")
    private String group;

    public KafkaProducer<String, String> kafkaProducer;

    public KafkaConsumer<String, String> kafkaConsumer;

    @Bean
    public KafkaConfig initKafka() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        if (needKafka) {
            kafkaConfig.kafkaProducer = kafkaProducerClient();
            kafkaConfig.kafkaConsumer = kafkaConsumerClient();
        }

        return kafkaConfig;
    }


    public KafkaProducer<String, String> kafkaProducerClient() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);

        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer(props);
        return producer;
    }


    public KafkaConsumer<String, String> kafkaConsumerClient() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrap);

        props.put("group.id", group);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", StringDeserializer.class);


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }


}
