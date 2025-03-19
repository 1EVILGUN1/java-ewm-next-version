package ewm.event.controller;

import ewm.client.EventClient;
import ewm.dto.event.EventDto;
import ewm.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class EventServiceController implements EventClient {

    private final EventService service;

    @Override
    public EventDto getEventById(@PathVariable Long eventId) {
        return service.publicGetEvent(eventId);
    }

    @Override
    public EventDto updateConfirmRequests(@PathVariable Long eventId, @RequestBody EventDto event) {
        return service.updateConfirmRequests(event);
    }

    @Override
    public EventDto getEventByInitiatorId(Long userId) {
        return service.getEventByInitiator(userId);
    }
}
