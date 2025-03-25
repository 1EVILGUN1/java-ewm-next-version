package ewm.controller;

import ewm.dto.request.RequestDto;
import ewm.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("users/{userId}/requests")
public class RequestPrivController {
    private final RequestService service;

    @GetMapping
    public List<RequestDto> getRequests(@PathVariable Long userId) {
        log.info("Получить запросы по userId --> {}", userId);
        return service.getRequests(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RequestDto createRequest(@PathVariable Long userId,
                                    @RequestParam Long eventId) {
        log.info("Создать запрос userId --> {}, eventId --> {}", userId, eventId);
        return service.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) {
        log.info("Отменить запрос по userId --> {}, requestId --> {}", userId, requestId);
        return service.cancelRequest(userId, requestId);
    }
}
