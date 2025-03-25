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
        log.info("Получить события, согласно устловиям -> {}", requestParams);
        return service.publicGetEvents(requestParams, request);
    }

    @GetMapping("/{id}")
    public UpdatedEventDto publicGetEvent(@PathVariable Long id, HttpServletRequest request) {
        return service.publicGetEvent(id, request);
    }

    @GetMapping("/recommendations")
    public List<RecommendationDto> getRecommendations(@RequestParam(defaultValue = "10") Long limit, HttpServletRequest request) {
        return service.getRecommendations(limit, request);
    }

    @PutMapping("/{eventId}/like")
    public void saveLike(@PathVariable Long eventId, HttpServletRequest request) {
        service.saveLike(eventId, request);
    }
}
