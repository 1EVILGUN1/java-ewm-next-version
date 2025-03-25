package ewm.event.controller.priv;

import ewm.dto.event.*;
import ewm.dto.request.RequestDto;
import ewm.event.service.EventService;
import ewm.event.service.validate.EventValidate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "users/{userId}/events")
@RequiredArgsConstructor
public class UserEventController {
    private final EventService service;

    @GetMapping
    List<EventDto> getEvents(@PathVariable Long userId,
                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                             @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получение событий для пользователя userId: {}, from: {}, size: {}", userId, from, size);
        List<EventDto> result = service.getEvents(userId, from, size);
        log.info("Успешно получено {} событий для пользователя userId: {}", result.size(), userId);
        return result;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    UpdatedEventDto createEvent(@PathVariable Long userId,
                                @Valid @RequestBody CreateEventDto event) {
        log.info("Создание события для пользователя userId: {}, данные: {}", userId, event);
        EventValidate.eventDateValidate(event, log);
        UpdatedEventDto result = service.createEvent(userId, event);
        log.info("Событие успешно создано с id: {} для пользователя userId: {}", result.getId(), userId);
        return result;
    }

    @GetMapping("/{id}")
    EventDto getEvent(@PathVariable Long userId,
                      @PathVariable Long id,
                      HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        log.info("Получение события id: {} для пользователя userId: {}, IP: {}, URI: {}", id, userId, ip, uri);
        EventDto result = service.getEventById(userId, id, ip, uri);
        log.info("Событие id: {} успешно получено для пользователя userId: {}", id, userId);
        return result;
    }

    @PatchMapping("/{eventId}")
    UpdatedEventDto updateEvent(@PathVariable Long userId,
                                @PathVariable Long eventId,
                                @Valid @RequestBody UpdateEventDto event) {
        log.info("Обновление события eventId: {} для пользователя userId: {}, данные: {}", eventId, userId, event);
        EventValidate.updateEventDateValidate(event, log);
        EventValidate.textLengthValidate(event, log);
        UpdatedEventDto result = service.updateEvent(userId, event, eventId);
        log.info("Событие eventId: {} успешно обновлено для пользователя userId: {}", eventId, userId);
        return result;
    }

    @GetMapping("/{eventId}/requests")
    List<RequestDto> getEventRequests(@PathVariable Long userId,
                                      @PathVariable Long eventId) {
        log.info("Получение запросов для события eventId: {} пользователя userId: {}", eventId, userId);
        List<RequestDto> result = service.getEventRequests(userId, eventId);
        log.info("Успешно получено {} запросов для события eventId: {} пользователя userId: {}", result.size(), eventId, userId);
        return result;
    }

    @PatchMapping("/{eventId}/requests")
    EventRequestStatusUpdateResult changeStatusEventRequests(@PathVariable Long userId,
                                                             @PathVariable Long eventId,
                                                             @RequestBody
                                                             @Valid EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса запросов для события eventId: {} пользователя userId: {}, данные: {}", eventId, userId, request);
        EventRequestStatusUpdateResult result = service.changeStatusEventRequests(userId, eventId, request);
        log.info("Статус запросов для события eventId: {} пользователя userId: {} успешно изменён", eventId, userId);
        return result;
    }
}