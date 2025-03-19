package ewm.mapper;


import ewm.client.UserClient;
import ewm.dto.subscription.SubscriptionDto;
import ewm.model.BlackList;
import ewm.model.Subscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {
    @Qualifier("ewm.client.UserClient")
    private final UserClient userClient;

    public SubscriptionDto subscribertoSubscriptionDto(List<Subscriber> subscriber) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setSubscribers(subscriber.stream()
                .map(Subscriber::getSubscriber)
                .map(userClient::getUserById)
                .collect(Collectors.toSet())
        );
        return dto;
    }

    public SubscriptionDto blackListSubscriptionDto(List<BlackList> blackList) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setBlackList(blackList.stream()
                .map(BlackList::getBlackList)
                .map(userClient::getUserById)
                .collect(Collectors.toSet())
        );
        return dto;
    }
}
