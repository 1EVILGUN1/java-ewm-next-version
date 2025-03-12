package ewm.event.controller;

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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    EventDto create(@PathVariable Long userId,
                    @Valid @RequestBody CreateEventDto event) {
        log.info("Получен запрос на создание события от пользователя с ID={}", userId);
        log.info("Данные нового события: {}", event);

        log.info("Проверка валидности даты события");
        EventValidate.eventDateValidate(event, log);
        log.info("Валидация даты успешно пройдена");

        EventDto createdEvent = service.create(userId, event);
        log.info("Событие успешно создано: {}", createdEvent);
        return createdEvent;
    }

    @PatchMapping("/{eventId}")
    EventDto update(@PathVariable Long userId,
                    @PathVariable Long eventId,
                    @Valid @RequestBody UpdateEventDto event) {
        log.info("Получен запрос на обновление события от пользователя ID={}", userId);
        log.info("ID события для обновления: {}, новые данные: {}", eventId, event);

        log.info("Проверка валидности даты события");
        EventValidate.updateEventDateValidate(event, log);
        log.info("Валидация даты успешно пройдена");

        log.info("Проверка длины текста события");
        EventValidate.textLengthValidate(event, log);
        log.info("Валидация длины текста успешно пройдена");

        EventDto updatedEvent = service.update(userId, event, eventId);
        log.info("Событие успешно обновлено: {}", updatedEvent);
        return updatedEvent;
    }

    @GetMapping("/{id}")
    EventDto getEvent(@PathVariable Long userId,
                      @PathVariable Long id,
                      HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        log.info("Получен запрос на получение события от пользователя ID={}", userId);
        log.info("ID события: {}, IP клиента: {}, URI: {}", id, ip, uri);

        EventDto event = service.getEventById(userId, id, ip, uri);
        log.info("Событие успешно найдено: {}", event);
        return event;
    }

    @GetMapping
    List<EventDto> getEvents(@PathVariable Long userId,
                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                             @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение списка событий от пользователя ID={}", userId);
        log.info("Параметры пагинации: from={}, size={}", from, size);

        List<EventDto> events = service.getEvents(userId, from, size);
        log.info("Найдено событий: {}", events.size());
        return events;
    }

    @GetMapping("/{eventId}/requests")
    List<RequestDto> getEventRequests(@PathVariable Long userId,
                                      @PathVariable Long eventId) {
        log.info("Получен запрос на получение заявок события от пользователя ID={}", userId);
        log.info("ID события: {}", eventId);

        List<RequestDto> requests = service.getEventRequests(userId, eventId);
        log.info("Найдено заявок: {}", requests.size());
        return requests;
    }

    @PatchMapping("/{eventId}/requests")
    EventRequestStatusUpdateResult changeStatusEventRequests(@PathVariable Long userId,
                                                             @PathVariable Long eventId,
                                                             @RequestBody
                                                             @Valid EventRequestStatusUpdateRequest request) {
        log.info("Получен запрос на изменение статуса заявок от пользователя ID={}", userId);
        log.info("ID события: {}, данные запроса: {}", eventId, request);

        EventRequestStatusUpdateResult result = service.changeStatusEventRequests(userId, eventId, request);
        log.info("Статусы заявок успешно обновлены: {}", result);
        return result;
    }
}