package ewm.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.grpc.stats.action.UserActionProto;
import ru.practicum.ewm.grpc.stats.collector.UserActionControllerGrpc;

@Component
public class CollectorClient {
    @GrpcClient("collector-service")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void collectUserAction(UserActionProto userActionProto) {
        client.collectUserAction(userActionProto);
    }
}
