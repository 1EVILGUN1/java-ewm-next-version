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
        log.info("Получение события по id: {}", eventId);
        EventDto result = service.publicGetEvent(eventId);
        log.info("Событие с id: {} успешно получено", eventId);
        return result;
    }

    @Override
    public EventDto updateConfirmRequests(@PathVariable Long eventId, @RequestBody EventDto event) {
        log.info("Обновление подтверждения запросов для события eventId: {}, данные: {}", eventId, event);
        EventDto result = service.updateConfirmRequests(event);
        log.info("Подтверждение запросов для события eventId: {} успешно обновлено", eventId);
        return result;
    }

    @Override
    public EventDto getEventByInitiatorId(Long userId) {
        log.info("Получение события по инициатору userId: {}", userId);
        EventDto result = service.getEventByInitiator(userId);
        log.info("Событие для инициатора userId: {} успешно получено", userId);
        return result;
    }
}