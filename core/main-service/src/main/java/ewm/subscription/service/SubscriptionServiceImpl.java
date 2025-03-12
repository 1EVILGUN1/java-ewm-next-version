package ewm.subscription.service;

import ewm.dto.event.EventDto;
import ewm.dto.subscription.SubscriptionDto;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.event.mapper.EventMapper;
import ewm.event.service.EventService;
import ewm.model.event.EventState;
import ewm.model.subscription.BlackList;
import ewm.model.subscription.Subscriber;
import ewm.model.user.User;
import ewm.subscription.mapper.SubscriptionMapper;
import ewm.subscription.repository.BlackListRepository;
import ewm.subscription.repository.SubscriberRepository;
import ewm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriberRepository subscriberRepository;
    private final BlackListRepository blackListRepository;
    private final UserService userService;
    private final EventService eventService;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    @Override
    public void addSubscriber(Subscriber subscriber) {
        log.debug("Проверка пользователя на существование в БД {}", subscriber.getUserId());
        User userSibscriber = getUser(subscriber.getUserId(), subscriber.getSubscriber());
        checkUserBD(subscriber.getUserId(), subscriber.getSubscriber());
        log.info("POST Запрос Сохранение пользователя в подписчиках {} {}", userSibscriber.getName(), userSibscriber.getEmail());
        subscriberRepository.save(subscriber);
    }

    @Transactional
    @Override
    public void addBlacklist(BlackList blackList) {
        log.debug("Проверка пользователей на существование в БД {}", blackList.getUserId());
        User blockUser = getUser(blackList.getUserId(), blackList.getBlackList());
        checkUserBD(blackList.getUserId(), blackList.getBlackList());
        log.info("POST Запрос Сохранение пользователя в черный список {} {}", blockUser.getName(), blockUser.getEmail());
        blackListRepository.save(blackList);
    }

    @Transactional
    @Override
    public void removeSubscriber(Long userId, Long subscriberId) {
        log.debug("Проверка пользователя на существование в БД {}", userId);
        getUser(userId, subscriberId);
        Optional<Subscriber> subscribed = subscriberRepository.findByUserIdAndSubscriber(userId, subscriberId);
        if (subscribed.isPresent()) {
            subscriberRepository.delete(subscribed.orElseThrow(() -> new NotFoundException("Пользователя нет в подписчиках")));
            log.info("DELETE Запрос на удаление пользователя из подписок выполнено");
        }
    }

    @Transactional
    @Override
    public void removeFromBlackList(long userId, long blackListId) {
        log.debug("Проверка пользователей на существование в БД {}", userId);
        getUser(userId, blackListId);
        Optional<BlackList> blackLists = blackListRepository.findByUserIdAndBlockUser(userId, blackListId);
        if (blackLists.isPresent()) {
            blackListRepository.delete(blackLists.orElseThrow(() -> new NotFoundException("Пользователя нет в черном листе")));
            log.info("DELETE Запрос на удаление пользователя из черного списка выполнено");
        }
    }

    @Override
    public SubscriptionDto getSubscribers(long userId) {
        log.debug("Получение списка ID пользователей на которых подписаны");
        List<Subscriber> subscriptions = subscriberRepository.findAllByUserId(userId);
        log.info("GET Запрос на получение списка подписок пользователя выполнен {}", subscriptions);
        return subscriptionMapper.subscribertoSubscriptionDto(subscriptions);
    }

    @Override
    public SubscriptionDto getBlacklists(long userId) {
        log.debug("Получение списка ID пользователей на которые в черном списке");
        List<BlackList> blackList = blackListRepository.findAllByUserId(userId);
        log.info("GET Запрос на получение списка черного списка пользователя выполнен {}", blackList);
        return subscriptionMapper.blackListSubscriptionDto(blackList);
    }

    @Override
    public List<EventDto> getEvents(long userId) {
        return subscriberRepository.findAllByUserId(userId).stream()
                .map(Subscriber::getSubscriber)
                .map(eventService::getEventByInitiator)
                .filter(Objects::nonNull)
                .filter(event -> event.getState().equals(EventState.PENDING)
                                 || event.getState().equals(EventState.PUBLISHED))
                .map(EventMapper::mapEventToEventDto)
                .toList();
    }

    private User getUser(long userId, long subscriberId) {
        userService.getUserFromRepo(userId);
        return userService.getUserFromRepo(subscriberId);
    }

    private void checkUserBD(long userId, long subscriberId) {
        if (subscriberRepository
                .findByUserIdAndSubscriber(userId, subscriberId)
                .isPresent()) {
            throw new ConflictException("Пользователь уже в списке подписчиков на данного человека");
        }
        if (blackListRepository
                .findByUserIdAndBlockUser(userId, subscriberId)
                .isPresent()) {
            throw new ConflictException("Пользователь находиться в черном списке и не может подписаться");
        }
    }
}
