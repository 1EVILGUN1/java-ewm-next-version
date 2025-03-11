package ewm.request.controller;

import ewm.client.request.RequestPrivClient;
import ewm.dto.request.RequestDto;
import ewm.request.service.RequestService;
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
public class RequestPrivateController implements RequestPrivClient {
    private final RequestService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RequestDto create(@PathVariable Long userId,
                             @RequestParam Long eventId) {
        log.info("Получен запрос на создание участия от пользователя с ID {} для события с ID {}", userId, eventId);
        RequestDto result = service.create(userId, eventId);
        log.info("Запрос на участие успешно создан для пользователя с ID {} и события с ID {}", userId, eventId);
        return result;
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable Long userId,
                                    @PathVariable Long requestId) {
        log.info("Получен запрос на отмену участия с ID {} от пользователя с ID {}", requestId, userId);
        RequestDto result = service.cancelRequest(userId, requestId);
        log.info("Запрос с ID {} успешно отменен для пользователя с ID {}", requestId, userId);
        return result;
    }

    @GetMapping
    public List<RequestDto> getRequests(@PathVariable Long userId) {
        log.info("Получен запрос на получение списка запросов на участие для пользователя с ID {}", userId);
        List<RequestDto> result = service.getRequests(userId);
        log.info("Получено {} запросов на участие для пользователя с ID {}", result.size(), userId);
        return result;
    }
}