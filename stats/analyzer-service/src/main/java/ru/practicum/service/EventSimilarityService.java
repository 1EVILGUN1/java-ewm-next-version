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
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventSimilarityService {

    // Константы
    private static final double LIKE_WEIGHT = 1.0;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double VIEW_WEIGHT = 0.4;
    private static final Logger LOGGER = Logger.getLogger(EventSimilarityService.class.getName());

    // Зависимости
    private final EventSimilarityRepository repository;
    private final UserActionService userActionService;

    // Методы работы с событиями
    public void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        EventSimilarity eventSimilarity = new EventSimilarity();
        eventSimilarity.setEventA(eventSimilarityAvro.getEventA());
        eventSimilarity.setEventB(eventSimilarityAvro.getEventB());
        eventSimilarity.setScore(eventSimilarityAvro.getScore());
        eventSimilarity.setEventTime(eventSimilarityAvro.getTimestamp());
        repository.save(eventSimilarity);
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<EventSimilarity> similarities = getSimilarByEventAndUserId(request.getEventId(), request.getUserId());
        return similarities.stream()
                .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                .limit(request.getMaxResults())
                .map(similarity -> buildRecommendedEventProto(similarity, request.getEventId()))
                .collect(Collectors.toList());
    }

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<UserAction> userActions = userActionService.getUserActionByUserId(request.getUserId());
        if (userActions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> actionWeights = calculateActionWeights(userActions);
        List<Long> eventIds = extractEventIds(userActions);
        List<UserAction> recentActions = getRecentUserActions(userActions, request.getMaxResults());

        Set<RecommendedEventProto> similarEvents = collectSimilarEvents(recentActions, request.getUserId());
        List<RecommendedEventProto> topSimilarEvents = getTopSimilarEvents(similarEvents, request.getMaxResults());

        return calculateFinalRecommendations(topSimilarEvents, eventIds, actionWeights, request.getMaxResults());
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<RecommendedEventProto> result = new ArrayList<>();
        for (Long eventId : request.getEventIdList()) {
            double totalScore = calculateEventInteractionScore(eventId);
            result.add(buildRecommendedEventProto(eventId, totalScore));
        }
        return result;
    }

    // Вспомогательные методы
    private List<EventSimilarity> getSimilarByEventAndUserId(Long eventId, Long userId) {
        List<Long> userEventIds = userActionService.getUserActionByUserId(userId).stream()
                .map(UserAction::getEventId)
                .toList();
        return repository.findByEventAOrEventB(eventId, eventId).stream()
                .filter(similarity -> !userEventIds.contains(similarity.getEventA()) || !userEventIds.contains(similarity.getEventB()))
                .toList();
    }

    private Map<Long, Double> calculateActionWeights(List<UserAction> userActions) {
        Map<Long, Double> weights = new HashMap<>();
        userActions.forEach(action -> weights.put(action.getEventId(), determineActionWeight(action.getActionType().toString())));
        return weights;
    }

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

    private List<Long> extractEventIds(List<UserAction> userActions) {
        return userActions.stream()
                .map(UserAction::getEventId)
                .toList();
    }

    private List<UserAction> getRecentUserActions(List<UserAction> userActions, long maxResults) {
        return userActions.stream()
                .sorted(Comparator.comparing(UserAction::getActionTime).reversed())
                .limit(maxResults)
                .toList();
    }

    private Set<RecommendedEventProto> collectSimilarEvents(List<UserAction> actions, Long userId) {
        Set<RecommendedEventProto> similarEvents = new HashSet<>();
        for (UserAction action : actions) {
            List<EventSimilarity> similarities = getSimilarByEventAndUserId(action.getEventId(), userId);
            similarEvents.addAll(similarities.stream()
                    .map(similarity -> buildRecommendedEventProto(similarity, action.getEventId(), 0.0))
                    .toList());
        }
        return similarEvents;
    }

    private List<RecommendedEventProto> getTopSimilarEvents(Set<RecommendedEventProto> events, long maxResults) {
        return events.stream()
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(maxResults)
                .toList();
    }

    private List<RecommendedEventProto> calculateFinalRecommendations(List<RecommendedEventProto> events,
                                                                      List<Long> userEventIds,
                                                                      Map<Long, Double> actionWeights,
                                                                      long maxResults) {
        List<RecommendedEventProto> result = new ArrayList<>();
        for (RecommendedEventProto event : events) {
            List<RecommendedEventProto> neighbors = getEventNeighbors(event.getEventId(), userEventIds);
            double weightedSum = calculateWeightedSum(neighbors, actionWeights, maxResults);
            double totalCoefficient = calculateTotalCoefficient(neighbors, maxResults);
            double finalScore = totalCoefficient != 0 ? weightedSum / totalCoefficient : 0.0;
            result.add(buildRecommendedEventProto(event.getEventId(), finalScore));
        }
        return result;
    }

    private List<RecommendedEventProto> getEventNeighbors(Long eventId, List<Long> userEventIds) {
        return repository.findByEventAOrEventB(eventId, eventId).stream()
                .filter(similarity -> userEventIds.contains(similarity.getEventB()) || userEventIds.contains(similarity.getEventA()))
                .map(similarity -> buildRecommendedEventProto(similarity, eventId))
                .toList();
    }

    private double calculateWeightedSum(List<RecommendedEventProto> neighbors, Map<Long, Double> actionWeights, long maxResults) {
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        neighbors.stream()
                .limit(maxResults)
                .forEach(neighbor -> sum.updateAndGet(v -> v + (neighbor.getScore() * actionWeights.getOrDefault(neighbor.getEventId(), 0.0))));
        return sum.get();
    }

    private double calculateTotalCoefficient(List<RecommendedEventProto> neighbors, long maxResults) {
        AtomicReference<Double> coefficient = new AtomicReference<>(0.0);
        neighbors.stream()
                .limit(maxResults)
                .forEach(neighbor -> coefficient.updateAndGet(v -> v + neighbor.getScore()));
        return coefficient.get();
    }

    private double calculateEventInteractionScore(Long eventId) {
        AtomicReference<Double> sum = new AtomicReference<>(0.0);
        userActionService.getUserActionByEventId(eventId).stream()
                .map(action -> determineActionWeight(action.getActionType().toString()))
                .forEach(weight -> sum.updateAndGet(v -> v + weight));
        return sum.get();
    }

    private RecommendedEventProto buildRecommendedEventProto(EventSimilarity similarity, Long baseEventId) {
        Long recommendedId = similarity.getEventA().equals(baseEventId) ? similarity.getEventB() : similarity.getEventA();
        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedId)
                .setScore(similarity.getScore())
                .build();
    }

    private RecommendedEventProto buildRecommendedEventProto(EventSimilarity similarity, Long baseEventId, double defaultScore) {
        Long recommendedId = similarity.getEventA().equals(baseEventId) ? similarity.getEventB() : similarity.getEventA();
        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedId)
                .setScore(defaultScore)
                .build();
    }

    private RecommendedEventProto buildRecommendedEventProto(Long eventId, double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }
}