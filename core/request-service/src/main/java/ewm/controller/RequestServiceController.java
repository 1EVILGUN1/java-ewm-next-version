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
        return service.updateAllRequests(requestDtoList);
    }

    @Override
    public RequestDto updateRequest(RequestDto requestDto) {
        return service.updateRequest(requestDto);
    }

    @Override
    public List<RequestDto> getRequestsByEventId(Long eventId) {
        return service.findAllByEventId(eventId);
    }

    @Override
    public List<RequestDto> findAllById(List<Long> ids) {
        return service.findAllById(ids);
    }

}
