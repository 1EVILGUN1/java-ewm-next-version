package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.recomendations.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.recomendations.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.UserPredictionsRequestProto;
import ru.practicum.service.EventSimilarityService;

import java.util.List;

@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final EventSimilarityService service;


    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> response = service.getSimilarEvents(request);
        response.stream().forEach(x -> responseObserver.onNext(x));
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> response = service.getInteractionsCount(request);
        response.stream().forEach(x -> responseObserver.onNext(x));
        responseObserver.onCompleted();
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> response = service.getRecommendationsForUser(request);
        response.stream().forEach(x -> responseObserver.onNext(x));
        responseObserver.onCompleted();
    }
}
