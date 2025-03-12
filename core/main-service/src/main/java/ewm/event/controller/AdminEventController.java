package ewm.event.controller;

import ewm.client.event.EventAdminClient;
import ewm.dto.event.AdminGetEventRequestDto;
import ewm.dto.event.EventDto;
import ewm.dto.event.UpdateEventDto;
import ewm.event.service.EventService;
import ewm.event.service.validate.EventValidate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("admin/events")
public class AdminEventController implements EventAdminClient {
    private final EventService service;

    @GetMapping
    public List<EventDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        log.info("Получен запрос на получение событий администратором");
        log.info("Параметры запроса событий: {}", requestParams);
        List<EventDto> events = service.adminGetEvents(requestParams);
        log.info("Найдено событий: {}, возвращаем результат", events.size());
        return events;
    }

    @PatchMapping("/{eventId}")
    public EventDto adminChangeEvent(@PathVariable Long eventId,
                                     @RequestBody @Valid UpdateEventDto eventDto) {
        log.info("Получен запрос на изменение события администратором");
        log.info("ID события для изменения: {}", eventId);
        log.info("Новые данные события: {}", eventDto);

        log.info("Начинаем валидацию даты события");
        EventValidate.updateEventDateValidate(eventDto, log);
        log.info("Валидация даты успешно пройдена");

        log.info("Начинаем валидацию длины текста");
        EventValidate.textLengthValidate(eventDto, log);
        log.info("Валидация длины текста успешно пройдена");

        log.info("Передаем запрос на обновление события в сервис");
        EventDto updatedEvent = service.adminChangeEvent(eventId, eventDto);
        log.info("Событие успешно обновлено, возвращаем результат: {}", updatedEvent);

        return updatedEvent;
    }
}