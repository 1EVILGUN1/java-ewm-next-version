package ewm.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.grpc.stats.analyzer.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.recomendations.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.recomendations.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.recomendations.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class RecommendationsClient {
    @GrpcClient("analyzer-service")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        // gRPC-метод getSimilarEvents возвращает Iterator, потому что в его схеме
        // мы указали, что он должен вернуть поток сообщений (stream stats.message.RecommendedEventProto)
        Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);

        // преобразуем Iterator в Stream
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(Long userId, Long maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> ids) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(ids)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
