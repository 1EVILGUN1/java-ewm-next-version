package ru.practicum.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.ewm.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.recomendations.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.recomendations.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.UserPredictionsRequestProto;
import ru.practicum.service.EventSimilarityService;

import java.util.List;

@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private static final Logger log = LoggerFactory.getLogger(RecommendationsController.class);
    private final EventSimilarityService service;

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получение похожих событий для запроса: {}", request);
            List<RecommendedEventProto> events = service.getSimilarEvents(request);
            events.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.debug("Успешно обработан запрос на похожие события, возвращено {} событий", events.size());
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный запрос на получение похожих событий: {}", request, e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Некорректные параметры запроса: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса на похожие события: {}", request, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка сервера: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получение количества взаимодействий для запроса: {}", request);
            List<RecommendedEventProto> events = service.getInteractionsCount(request);
            events.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.debug("Успешно обработан запрос на количество взаимодействий, возвращено {} событий", events.size());
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный запрос на получение количества взаимодействий: {}", request, e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Некорректные параметры запроса: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса на количество взаимодействий: {}", request, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка сервера: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получение рекомендаций для пользователя по запросу: {}", request);
            List<RecommendedEventProto> events = service.getRecommendationsForUser(request);
            events.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
            log.debug("Успешно обработан запрос на рекомендации для пользователя, возвращено {} событий", events.size());
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный запрос на получение рекомендаций для пользователя: {}", request, e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Некорректные параметры запроса: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса на рекомендации для пользователя: {}", request, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Внутренняя ошибка сервера: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}