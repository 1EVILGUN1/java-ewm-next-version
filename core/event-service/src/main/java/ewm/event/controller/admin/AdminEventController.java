package ewm.event.controller.admin;

import ewm.dto.event.AdminGetEventRequestDto;
import ewm.dto.event.UpdateEventDto;
import ewm.dto.event.UpdatedEventDto;
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
public class AdminEventController {
    private final EventService service;

    @GetMapping
    public List<UpdatedEventDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        log.info("Получить события, согласно условиям -> {}", requestParams);
        List<UpdatedEventDto> result = service.adminGetEvents(requestParams);
        log.info("Успешно получено {} событий", result.size());
        return result;
    }

    @PatchMapping("/{eventId}")
    public UpdatedEventDto adminChangeEvent(@PathVariable Long eventId,
                                            @RequestBody @Valid UpdateEventDto eventDto) {
        log.info("Изменить событие eventId = {}, поля -> {}", eventId, eventDto);
        EventValidate.updateEventDateValidate(eventDto, log);
        EventValidate.textLengthValidate(eventDto, log);
        UpdatedEventDto result = service.adminChangeEvent(eventId, eventDto);
        log.info("Событие с id: {} успешно обновлено", eventId);
        return result;
    }
}