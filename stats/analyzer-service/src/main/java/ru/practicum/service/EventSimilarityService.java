package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.recomendations.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.recomendations.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilarityRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class EventSimilarityService {

    private final EventSimilarityRepository repository;
    private final UserActionService userActionService;

    public void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        EventSimilarity eventSimilarity = new EventSimilarity();
        eventSimilarity.setEventA(eventSimilarityAvro.getEventA());
        eventSimilarity.setEventB(eventSimilarityAvro.getEventB());
        eventSimilarity.setScore(eventSimilarityAvro.getScore());
        eventSimilarity.setEventTime(eventSimilarityAvro.getTimestamp());
        repository.save(eventSimilarity);
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {

        List<EventSimilarity> list = getSimilarByEventAndUserId(request.getEventId(), request.getUserId());
        list.sort((s1, s2) -> s2.getScore().compareTo(s1.getScore()));
        return list.stream()
                .limit(request.getMaxResults())
                .map(x -> {
                    Long id = x.getEventA();
                    if (id == request.getEventId()) id = x.getEventB();
                    RecommendedEventProto e = RecommendedEventProto.newBuilder()
                            .setEventId(id)
                            .setScore(x.getScore())
                            .build();
                    return e;
                })
                .toList();

    }

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        // недавно просмотренные события
        List<UserAction> userActionList = userActionService.getUserActionByUserId(request.getUserId());
        List<Long> ids = userActionList.stream()
                .map(x -> x.getEventId()).toList();
        Map<Long, Double> kMap = new HashMap<>();
        userActionList.stream()
                .forEach(x -> {
                    double weiht = 0;
                    switch (x.getActionType()) {
                        case LIKE -> weiht = 1;
                        case REGISTER -> weiht = 0.8;
                        case VIEW -> weiht = 0.4;
                    }
                    kMap.put(x.getEventId(), weiht);
                });
        if (userActionList.size() == 0) return new ArrayList<>();
        userActionList.sort((s1, s2) -> s2.getActionTime().compareTo(s1.getActionTime()));
        List<UserAction> actionList = userActionList.stream().limit(request.getMaxResults()).toList();
        // новые похожие события
        Set<RecommendedEventProto> list = new HashSet<>();
        for (UserAction ua : actionList) {
            list.addAll(getSimilarByEventAndUserId(ua.getEventId(), ua.getUserId())
                    .stream()
                    .map(x -> {
                        long id = x.getEventB();
                        if (!x.getEventA().equals(ua.getEventId())) id = x.getEventA();
                        return RecommendedEventProto.newBuilder()
                                .setEventId(id)
                                .setScore(0)
                                .build();
                    }).toList());
        }
        // N самых похожих событий
        List<RecommendedEventProto> recList = list.stream()
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();

        List<RecommendedEventProto> result = new ArrayList<>();
        for (RecommendedEventProto rep : recList) {
            // ближайшие соседи
            List<RecommendedEventProto> nList = repository.findByEventAOrEventB(rep.getEventId(), rep.getEventId()).stream()
                    .filter(x -> ids.contains(x.getEventB()) || ids.contains(x.getEventA()))
                    .map(x -> {
                        long id = x.getEventB();
                        if (x.getEventA() != rep.getEventId()) id = x.getEventA();
                        return RecommendedEventProto.newBuilder()
                                .setEventId(id)
                                .setScore(x.getScore())
                                .build();
                    })
                    .toList();


            AtomicReference<Double> sum = new AtomicReference<>(0d);
            AtomicReference<Double> koef = new AtomicReference<>(0d);
            nList.stream().limit(request.getMaxResults())
                    .forEach(x -> sum.set(sum.get() + (x.getScore() * kMap.get(x.getEventId()))));
            nList.stream().limit(request.getMaxResults())
                    .forEach(x -> koef.set(koef.get() + x.getScore()));
            result.add(RecommendedEventProto.newBuilder()
                    .setEventId(rep.getEventId())
                    .setScore(sum.get() / koef.get())
                    .build());
        }
        return result;
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<RecommendedEventProto> result = new ArrayList<>();
        for (Long id : request.getEventIdList()) {
            AtomicReference<Double> sum = new AtomicReference<>(0d);
            List<UserAction> userActionList = userActionService.getUserActionByEventId(id);
            userActionList.stream()
                    .map(x -> {
                        double weiht = 0;
                        switch (x.getActionType()) {
                            case LIKE -> weiht = 1;
                            case REGISTER -> weiht = 0.8;
                            case VIEW -> weiht = 0.4;
                        }
                        return weiht;
                    })
                    .forEach(x -> sum.set(sum.get() + x));
            result.add(RecommendedEventProto.newBuilder()
                    .setEventId(id)
                    .setScore(sum.get())
                    .build());
        }
        return result;
    }

    private List<EventSimilarity> getSimilarByEventAndUserId(Long eventId, Long userId) {

        List<Long> ids = userActionService.getUserActionByUserId(userId).stream()
                .map(x -> x.getEventId()).toList();
        return repository.findByEventAOrEventB(eventId, eventId).stream()
                .filter(x -> !ids.contains(x.getEventA()) || !ids.contains(x.getEventB()))
                .toList();

    }
}
