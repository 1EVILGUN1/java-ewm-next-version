package ewm.service;

import ewm.dto.request.RequestDto;

import java.util.List;

// Управление заявками
public interface RequestService {
    List<RequestDto> getRequests(Long userId);

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> findAllById(List<Long> ids);

    List<RequestDto> findAllByEventId(Long eventId);

    RequestDto updateRequest(RequestDto requestDto);

    List<RequestDto> updateAllRequests(List<RequestDto> requestDtoList);
}
