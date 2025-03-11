package ewm.request.service;

import ewm.dto.request.RequestDto;

import java.util.List;

public interface RequestService {

    RequestDto create(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getRequests(Long userId);
}
