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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AggregatorService {
    private Map<Long, Map<Long, Double>> eventWeight;
    private Map<Long, Double> eventWeightSum;
    private Map<Long, Map<Long, Double>> minWeightsSum;

    @Value("${kafka.topic-sums}")
    private String topic;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public AggregatorService() {
        eventWeight = new HashMap<>();
        eventWeightSum = new HashMap<>();
        minWeightsSum = new HashMap<>();
    }

    public void calculateWeight(UserActionAvro userActionAvro) {
        double weiht = 0;
        switch (userActionAvro.getActionType()) {
            case LIKE -> weiht = 1;
            case REGISTER -> weiht = 0.8;
            case VIEW -> weiht = 0.4;
        }
        if (eventWeight.containsKey(userActionAvro.getEventId())) {
            if (eventWeight.get(userActionAvro.getEventId()).containsKey(userActionAvro.getUserId())) {
                eventWeight.get(userActionAvro.getEventId())
                        .put(userActionAvro.getUserId(),
                                Math.min(weiht, eventWeight.get(userActionAvro.getEventId()).get(userActionAvro.getUserId())));
            } else return;
        } else {
            eventWeight.put(userActionAvro.getEventId(), new HashMap<>());
            eventWeight.get(userActionAvro.getEventId()).put(userActionAvro.getUserId(), weiht);
        }

        AtomicReference<Double> sum = new AtomicReference<>(0d);
        eventWeight.get(userActionAvro.getEventId()).values().stream()
                .forEach(x -> sum.set(sum.get() + x));

        eventWeightSum.put(userActionAvro.getEventId(), sum.get());


        for (Long l : eventWeight.keySet()) {
            if (l == userActionAvro.getEventId()) continue;
            if (eventWeight.get(l).containsKey(userActionAvro.getUserId())) {
                Double minW = Math.min(eventWeight.get(l).get(userActionAvro.getUserId()),
                        eventWeight.get(userActionAvro.getEventId()).get(userActionAvro.getUserId()));
                put(userActionAvro.getEventId(), l, minW);

                send(userActionAvro.getEventId(), l, minW, userActionAvro.getTimestamp());
            }
        }
    }

    public void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void send(long eventA, long eventB, double sum, Instant instant) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        EventSimilarityAvro eventSimilarityAvro = new EventSimilarityAvro(first, second, sum, instant);
        ProducerRecord<String, EventSimilarityAvro> eventSimilarityRecord = new ProducerRecord<>(topic, eventSimilarityAvro);
        kafkaTemplate.send(eventSimilarityRecord);
    }
}
