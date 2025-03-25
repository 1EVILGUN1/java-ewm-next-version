package ewm.event.controller.pub;

import ewm.dto.event.PublicGetEventRequestDto;
import ewm.dto.event.RecommendationDto;
import ewm.dto.event.UpdatedEventDto;
import ewm.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final EventService service;

    @GetMapping
    public List<UpdatedEventDto> publicGetEvents(HttpServletRequest request, PublicGetEventRequestDto requestParams) {
        log.info("Получить события, согласно условиям -> {}", requestParams);
        List<UpdatedEventDto> result = service.publicGetEvents(requestParams, request);
        log.info("Успешно получено {} событий", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public UpdatedEventDto publicGetEvent(@PathVariable Long id, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        log.info("Получение события с id: {}, IP: {}, URI: {}", id, ip, uri);
        UpdatedEventDto result = service.publicGetEvent(id, request);
        log.info("Событие с id: {} успешно получено", id);
        return result;
    }

    @GetMapping("/recommendations")
    public List<RecommendationDto> getRecommendations(@RequestParam(defaultValue = "10") Long limit, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log.info("Получение рекомендаций с лимитом: {}, IP: {}", limit, ip);
        List<RecommendationDto> result = service.getRecommendations(limit, request);
        log.info("Успешно получено {} рекомендаций", result.size());
        return result;
    }

    @PutMapping("/{eventId}/like")
    public void saveLike(@PathVariable Long eventId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log.info("Сохранение лайка для события eventId: {}, IP: {}", eventId, ip);
        service.saveLike(eventId, request);
        log.info("Лайк для события eventId: {} успешно сохранён", eventId);
    }
}