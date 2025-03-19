package ewm.client;

import ewm.dto.event.EventDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/event/{eventId}")
    EventDto getEventById(@PathVariable Long eventId);

    @PostMapping("/event/{eventId}")
    EventDto updateConfirmRequests(@PathVariable Long eventId, @RequestBody EventDto event);

    @GetMapping("/event-subscription/{userId}")
    EventDto getEventByInitiatorId(@PathVariable Long userId);

}
