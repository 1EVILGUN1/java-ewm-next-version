package ewm.client.subscription;

import ewm.dto.event.EventDto;
import ewm.dto.subscription.SubscriptionDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "subscription-client")
public interface SubscriptionClient {
    @PostMapping("/subscriptions/{subscriberId}")
    void addSubscribe(@PathVariable("userId") @Positive @NotNull long userId,
                      @PathVariable("subscriberId") @Positive @NotNull long subscriberId);

    @PostMapping("black-list/{blackListId}")
    void addBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                      @PathVariable("blackListId") @Positive @NotNull long blackListId);

    @DeleteMapping("/subscriptions/{subscriberId}")
    void removeSubscriber(@PathVariable("userId") @Positive @NotNull long userId,
                          @PathVariable("subscriberId") @Positive @NotNull long subscriberId);

    @DeleteMapping("/black-list/{blackListId}")
    void removeBlackList(@PathVariable("userId") @Positive @NotNull long userId,
                         @PathVariable("blackListId") @Positive @NotNull long blackListId);

    @GetMapping("/subscriptions")
    SubscriptionDto getListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId);

    @GetMapping("/black-list")
    SubscriptionDto getBlackListSubscriptions(@PathVariable("userId") @Positive @NotNull long userId);

    @GetMapping("/subscriptions/events")
    List<EventDto> getEventsSubscriptions(@PathVariable("userId") @Positive @NotNull long userId);

}
