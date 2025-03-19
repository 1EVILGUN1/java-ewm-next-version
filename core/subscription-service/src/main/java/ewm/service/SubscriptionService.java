package ewm.service;


import ewm.dto.subscription.SubscriptionDto;
import ewm.model.BlackList;
import ewm.model.Event;
import ewm.model.Subscriber;

import java.util.List;

public interface SubscriptionService {
    void addSubscriber(Subscriber subscriber);

    void addBlacklist(BlackList blackList);

    void removeSubscriber(Long userId, Long subscriberId);

    SubscriptionDto getSubscribers(long userId);

    SubscriptionDto getBlacklists(long userId);

    List<Event> getEvents(long userId);

    void removeFromBlackList(long userId, long blackListId);
}
