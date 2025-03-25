package ewm.controller;

import ewm.client.RequestOperations;
import ewm.dto.request.RequestDto;
import ewm.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class RequestServiceController implements RequestOperations {
    private final RequestService service;

    @Override
    public List<RequestDto> updateAllRequest(List<RequestDto> requestDtoList) {
        log.info("Обновление всех запросов, количество: {}", requestDtoList.size());
        List<RequestDto> result = service.updateAllRequests(requestDtoList);
        log.info("Успешно обновлено {} запросов", result.size());
        return result;
    }

    @Override
    public RequestDto updateRequest(RequestDto requestDto) {
        log.info("Обновление запроса: {}", requestDto);
        RequestDto result = service.updateRequest(requestDto);
        log.info("Запрос с id: {} успешно обновлён", result.getId());
        return result;
    }

    @Override
    public List<RequestDto> getRequestsByEventId(Long eventId) {
        log.info("Получение запросов по eventId: {}", eventId);
        List<RequestDto> result = service.findAllByEventId(eventId);
        log.info("Успешно получено {} запросов для eventId: {}", result.size(), eventId);
        return result;
    }

    @Override
    public List<RequestDto> findAllById(List<Long> ids) {
        log.info("Получение запросов по id: {}", ids);
        List<RequestDto> result = service.findAllById(ids);
        log.info("Успешно получено {} запросов по указанным id", result.size());
        return result;
    }
}