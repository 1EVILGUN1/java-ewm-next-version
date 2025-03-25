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
        List<RequestDto> result = service.getRequests(userId);
        log.info("Успешно получено {} запросов для userId: {}", result.size(), userId);
        return result;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RequestDto createRequest(@PathVariable Long userId,
                                    @RequestParam Long eventId) {
        log.info("Создать запрос userId --> {}, eventId --> {}", userId, eventId);
        RequestDto result = service.createRequest(userId, eventId);
        log.info("Запрос успешно создан с id: {} для userId: {} и eventId: {}", result.getId(), userId, eventId);
        return result;
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) {
        log.info("Отменить запрос по userId --> {}, requestId --> {}", userId, requestId);
        RequestDto result = service.cancelRequest(userId, requestId);
        log.info("Запрос requestId: {} успешно отменён для userId: {}", requestId, userId);
        return result;
    }
}