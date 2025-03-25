package ewm.event.controller.pub;

import ewm.dto.event.PublicGetEventRequestDto;
import ewm.dto.event.RecommendationDto;
import ewm.dto.event.UpdatedEventDto;
import ewm.event.service.EventService;
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
@RequestMapping("events")
public class PublicEventController {
    private static final String USER_ID_HEADER = "X-EWM-USER-ID";
    private final EventService service;

    @GetMapping
    public List<UpdatedEventDto> publicGetEvents(@RequestHeader(value = USER_ID_HEADER, required = false) final long userId,
                                                 @Valid PublicGetEventRequestDto requestParams) {
        log.info("Получить события, согласно условиям -> {}", requestParams);
        List<UpdatedEventDto> result = service.publicGetEvents(requestParams, userId);
        log.info("Успешно получено {} событий", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public UpdatedEventDto publicGetEvent(@PathVariable Long id, @RequestHeader(value = USER_ID_HEADER, required = false) final long userId) {
        log.info("Получение события с id: {}, пользователь: {}", id, userId);
        UpdatedEventDto result = service.publicGetEvent(id, userId);
        log.info("Событие с id: {} успешно получено", id);
        return result;
    }

    @GetMapping("/recommendations")
    public List<RecommendationDto> getRecommendations(@RequestParam(defaultValue = "10") Long limit,
                                                      @RequestHeader(value = USER_ID_HEADER, required = false) final long userId) {
        log.info("Получение рекомендаций с лимитом: {}, пользователь: {}", limit, userId);
        List<RecommendationDto> result = service.getRecommendations(limit, userId);
        log.info("Успешно получено {} рекомендаций", result.size());
        return result;
    }

    @PutMapping("/{eventId}/like")
    public void saveLike(@PathVariable Long eventId, @RequestHeader(USER_ID_HEADER) final long userId) {
        log.info("Сохранение лайка для события eventId: {}, пользователь: {}", eventId, userId);
        service.saveLike(eventId, userId);
        log.info("Лайк для события eventId: {} успешно сохранён", eventId);
    }
}