package ewm.subscription.mapper;


import ewm.dto.subscription.SubscriptionDto;
import ewm.model.subscription.BlackList;
import ewm.model.subscription.Subscriber;
import ewm.user.mapper.UserMapper;
import ewm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {
    private final UserService userService;

    public SubscriptionDto subscribertoSubscriptionDto(List<Subscriber> subscriber) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setSubscribers(subscriber.stream()
                .map(Subscriber::getSubscriber)
                .map(userService::getUserFromRepo)
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toSet())
        );
        return dto;
    }

    public SubscriptionDto blackListSubscriptionDto(List<BlackList> blackList) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setBlackList(blackList.stream()
                .map(BlackList::getBlackList)
                .map(userService::getUserFromRepo)
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toSet())
        );
        return dto;
    }
}
