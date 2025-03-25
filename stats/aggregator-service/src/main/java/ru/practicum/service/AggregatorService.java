package ru.practicum.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Service
public class AggregatorService {

    // Константы
    private static final double LIKE_WEIGHT = 1.0;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double VIEW_WEIGHT = 0.4;
    private static final Logger LOGGER = Logger.getLogger(AggregatorService.class.getName());
    // Коллекции с потокобезопасностью
    private final Map<Long, Map<Long, Double>> eventWeight;
    private final Map<Long, Double> eventWeightSum;
    private final Map<Long, Map<Long, Double>> minWeightsSum;
    // Зависимости
    @Value("${kafka.topic-sums}")
    private String topic;
    @Autowired
    private KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    // Конструктор
    public AggregatorService() {
        this.eventWeight = new ConcurrentHashMap<>();
        this.eventWeightSum = new ConcurrentHashMap<>();
        this.minWeightsSum = new ConcurrentHashMap<>();
    }

    // Основной метод расчёта весов
    public void calculateWeight(UserActionAvro userActionAvro) {
        double weight = determineActionWeight(userActionAvro.getActionType().toString());
        updateEventWeight(userActionAvro.getEventId(), userActionAvro.getUserId(), weight);
        updateEventWeightSum(userActionAvro.getEventId());
        calculateAndSendSimilarities(userActionAvro);
    }

    // Метод получения веса для события
    public double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return minWeightsSum.computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    // Вспомогательные методы
    private double determineActionWeight(String actionType) {
        return switch (actionType) {
            case "LIKE" -> LIKE_WEIGHT;
            case "REGISTER" -> REGISTER_WEIGHT;
            case "VIEW" -> VIEW_WEIGHT;
            default -> {
                LOGGER.warning("Неизвестный тип действия: " + actionType);
                yield 0.0;
            }
        };
    }

    private void updateEventWeight(Long eventId, Long userId, double weight) {
        Map<Long, Double> userWeights = eventWeight.computeIfAbsent(eventId, k -> new HashMap<>());
        if (userWeights.containsKey(userId)) {
            userWeights.put(userId, Math.min(weight, userWeights.get(userId)));
        } else {
            userWeights.put(userId, weight);
        }
    }

    private void updateEventWeightSum(Long eventId) {
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        eventWeight.get(eventId).values().forEach(value -> sum.updateAndGet(current -> current + value));
        eventWeightSum.put(eventId, sum.get());
    }

    private void calculateAndSendSimilarities(UserActionAvro userActionAvro) {
        Long eventId = userActionAvro.getEventId();
        Long userId = userActionAvro.getUserId();

        for (Long otherEventId : eventWeight.keySet()) {
            if (otherEventId.equals(eventId)) {
                continue;
            }
            if (eventWeight.get(otherEventId).containsKey(userId)) {
                double minWeight = Math.min(
                        eventWeight.get(otherEventId).get(userId),
                        eventWeight.get(eventId).get(userId)
                );
                put(eventId, otherEventId, minWeight);
                send(eventId, otherEventId, minWeight, userActionAvro.getTimestamp());
            }
        }
    }

    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        minWeightsSum.computeIfAbsent(first, e -> new HashMap<>()).put(second, sum);
    }

    private void send(long eventA, long eventB, double sum, Instant instant) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        EventSimilarityAvro eventSimilarityAvro = new EventSimilarityAvro(first, second, sum, instant);
        ProducerRecord<String, EventSimilarityAvro> record = new ProducerRecord<>(topic, eventSimilarityAvro);
        kafkaTemplate.send(record);
    }
}