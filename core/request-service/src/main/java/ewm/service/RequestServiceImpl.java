package ewm.service;

import ewm.client.EventClient;
import ewm.client.UserClient;
import ewm.dto.event.EventDto;
import ewm.dto.request.RequestDto;
import ewm.dto.user.UserDto;
import ewm.enums.EventState;
import ewm.enums.RequestStatus;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.mapper.ReqMapper;
import ewm.model.Request;
import ewm.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j

@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<RequestDto> getRequests(Long userId) {
        userClient.getUserById(userId);
        return ReqMapper.mapListRequests(requestRepository.findAllByRequesterId(userId));
    }

    @Transactional
    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        EventDto event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new ConflictException("Нет события в нужном статусе");
        }
        UserDto user = userClient.getUserById(userId);
        checkRequest(userId, event);
        Request request = Request.builder()
                .requesterId(userId)
                .created(LocalDateTime.now())
                .status(!event.getRequestModeration()
                        || event.getParticipantLimit() == 0
                        ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .eventId(eventId)
                .build();
        request = requestRepository.save(request);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventClient.updateConfirmRequests(eventId, event);
        }
        return ReqMapper.mapToRequestDto(request);
    }

    @Transactional
    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        UserDto user = userClient.getUserById(userId);
        Request request = getRequest(requestId);
        if (!request.getRequesterId().equals(userId))
            throw new ConflictException("Другой пользователь не может отменить запрос");
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        EventDto event = eventClient.getEventById(request.getEventId());
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventClient.updateConfirmRequests(event.getId(), event);
        return ReqMapper.mapToRequestDto(request);
    }

    public List<RequestDto> findAllById(List<Long> ids) {

        return ReqMapper.mapListRequests(requestRepository.findAllById(ids));
    }

    public List<RequestDto> findAllByEventId(Long eventId) {
        return ReqMapper.mapListRequests(requestRepository.findAllByEventId(eventId));
    }

    public RequestDto updateRequest(RequestDto requestDto) {
        Request request = ReqMapper.mapDtoToRequest(requestDto);
        request = requestRepository.save(request);
        return ReqMapper.mapToRequestDto(request);
    }

    public List<RequestDto> updateAllRequests(List<RequestDto> requestDtoList) {
        List<RequestDto> list = new ArrayList<>(requestDtoList.size());
        for (RequestDto r : requestDtoList) {
            list.add(updateRequest(r));
        }
        return list;
    }

    private void checkRequest(Long userId, EventDto event) {
        if (!requestRepository.findAllByRequesterIdAndEventId(userId, event.getId()).isEmpty())
            throw new ConflictException("нельзя добавить повторный запрос");
        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии");
        if (!event.getState().equals(EventState.PUBLISHED.toString()))
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests()))
            throw new ConflictException("у события достигнут лимит запросов на участие");
    }

    private Request getRequest(Long requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isEmpty())
            throw new NotFoundException("Запроса с id = " + requestId.toString() + " не существует");
        return request.get();
    }


}
