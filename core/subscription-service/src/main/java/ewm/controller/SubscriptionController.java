package ewm.controller;

import ewm.dto.subscription.SubscriptionDto;
import ewm.error.exception.ConflictException;
import ewm.model.BlackList;
import ewm.model.Event;
import ewm.model.Subscriber;
import ewm.service.SubscriptionService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService service;

    @PostMapping("/subscriptions/{subscriberId}")
    public void addSubscribe(@PathVariable("userId") @Positive @NotNull long userId,
                             @PathVariable("subscriberId") @Positive @NotNull long subscriberId) {
        if (userId == subscriberId) {
            throw new ConflictException("Пользователь не может подписаться сам на себя");
        }
        log.info("POST Запрос на добавление подписки на человека  >>");
        Subscriber subscriber = new Subscriber();
        subscriber.setUserId(userId);
        subscriber.setSubscriber(subscriberId);
        service.addSubscriber(subscriber);
    }

    @PostMapping("black-list/{blackListId}")
    public void addBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                             @PathVariable("blackListId") @Positive @NotNull long blackListId) {
        log.info("POST Запрос на добавление человека в черный список >>");
        if (userId == blackListId) {
            throw new ConflictException("Пользователь не может добавить в черный список сам на себя");
        }

        BlackList blackList = new BlackList();
        blackList.setUserId(userId);
        blackList.setBlackList(blackListId);
        service.addBlacklist(blackList);

    }

    @DeleteMapping("/subscriptions/{subscriberId}")
    public void removeSubscriber(@PathVariable("userId") @Positive @NotNull long userId,
                                 @PathVariable("subscriberId") @Positive @NotNull long subscriberId) {
        log.info("DELETE Запрос на удаление человека из списка подписок >>");
        service.removeSubscriber(userId, subscriberId);
    }

    @DeleteMapping("/black-list/{blackListId}")
    public void removeBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                                @PathVariable("blackListId") @Positive @NotNull long blackListId) {
        log.info("DELETE Запрос на удаление человека из черного списка >>");
        service.removeFromBlackList(userId, blackListId);
    }

    @GetMapping("/subscriptions")
    public SubscriptionDto getListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        log.info("GET Запрос на получение списка подписок человека с ID {}", userId);
        SubscriptionDto subscriptionDto = service.getSubscribers(userId);
        log.info("GET Запрос {}", subscriptionDto);
        return subscriptionDto;
    }

    @GetMapping("/black-list")
    public SubscriptionDto getBlackListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        log.info("GET Запрос на получение черного списка человека с ID {}", userId);
        SubscriptionDto subscriptionDto = service.getBlacklists(userId);
        log.info("GET Запрос {}", subscriptionDto);
        return subscriptionDto;
    }

    @GetMapping("/subscriptions/events")
    public List<Event> getEventsSubscriptions(@PathVariable("userId") @Positive @NotNull long userId) {
        log.info("GET Запрос на получение списка мероприятий пользователей на которых подписан человек с ID {} ", userId);
        List<Event> eventShortResponseDtos = service.getEvents(userId);
        log.info("GET Запрос на получение списка мероприятий выполнен {} ", eventShortResponseDtos);
        return eventShortResponseDtos;
    }
}
