package ru.practicum.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.serialize.EventSimilaritySerializer;

import java.util.HashMap;
import java.util.Map;

// Конфигурация Kafka Producer для отправки данных о схожести событий
@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-server}")
    private String bootstrapServer;
    @Value("${kafka.client-id}")
    private String clientId;
    @Value("${kafka.group-id}")
    private String groupId;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = createProducerConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Создание конфигурации продюсера
    private Map<String, Object> createProducerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class); // Ключи не используются
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventSimilaritySerializer.class);
        // Дополнительные настройки для надёжности
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Гарантия доставки
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Повторные попытки
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Размер батча
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Задержка отправки
        return configProps;
    }
}
