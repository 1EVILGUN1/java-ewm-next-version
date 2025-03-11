package ewm.event.controller;

import ewm.client.event.EventPubClient;
import ewm.dto.event.EventDto;
import ewm.dto.event.PublicGetEventRequestDto;
import ewm.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("events")
public class PublicEventController implements EventPubClient {
    private final EventService service;

    @GetMapping
    public List<EventDto> publicGetEvents(HttpServletRequest request, PublicGetEventRequestDto requestParams) {
        log.info("Получен публичный запрос на получение списка событий");
        log.info("Параметры запроса событий: {}", requestParams);
        log.info("Информация о запросе: IP={}, URI={}",
                request.getRemoteAddr(),
                request.getRequestURI());

        List<EventDto> events = service.publicGetEvents(requestParams, request);
        log.info("Найдено событий: {}, возвращаем результат", events.size());

        return events;
    }

    @GetMapping("/{id}")
    public EventDto publicGetEvent(@PathVariable Long id,
                                   HttpServletRequest request) {
        log.info("Получен публичный запрос на получение события с ID={}", id);
        log.info("Информация о запросе: IP={}, URI={}",
                request.getRemoteAddr(),
                request.getRequestURI());

        EventDto event = service.publicGetEvent(id, request);
        log.info("Событие с ID={} успешно найдено: {}", id, event);

        return event;
    }
}