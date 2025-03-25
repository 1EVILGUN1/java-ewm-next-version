package ru.practicum.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.EventSimilarityService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityListener {

    private final EventSimilarityService service;

    @KafkaListener(topics = "${kafka.topic-sums}",
            containerFactory = "userActionListenerContainerFactory",
            autoStartup = "true")
    public void listen(EventSimilarityAvro message) {
        log.info("Получено сообщение: {}", message.toString());
        service.saveEventSimilarity(message);
    }
}
