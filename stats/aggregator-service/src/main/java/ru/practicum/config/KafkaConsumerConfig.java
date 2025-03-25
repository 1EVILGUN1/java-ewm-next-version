package ru.practicum.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.serialize.UserActionDeserializer;

import java.util.HashMap;
import java.util.Map;

// Конфигурация Kafka Consumer для обработки действий пользователей (UserActionAvro)
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${kafka.bootstrap-server}")
    private String bootstrapServer;
    @Value("${kafka.client-id}")
    private String clientId;
    @Value("${kafka.group-id}")
    private String groupId;


    @Bean
    public ConsumerFactory<String, UserActionAvro> consumerFactory() {
        Map<String, Object> props = createConsumerConfig();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // Общий метод для создания конфигурации потребителя
    private Map<String, Object> createConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        // Дополнительные настройки для надёжности
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Начинать с начала, если нет оффсета
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Ограничение записей за один poll
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Ручное управление коммитами
        return props;
    }
}
