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
    private final RequestRepository repository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<RequestDto> getRequests(Long userId) {
        log.info("Получение запросов для пользователя userId: {}", userId);
        userClient.getUserById(userId);
        List<RequestDto> result = ReqMapper.mapListRequests(repository.findAllByRequesterId(userId));
        log.info("Успешно получено {} запросов для userId: {}", result.size(), userId);
        return result;
    }

    @Transactional
    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание запроса для userId: {}, eventId: {}", userId, eventId);
        EventDto event = eventClient.getEventById(eventId);
        if (event == null) {
            log.warn("Событие eventId: {} не найдено или не в нужном статусе", eventId);
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
        request = repository.save(request);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventClient.updateConfirmRequests(eventId, event);
            log.debug("Подтверждён запрос для события eventId: {}, новый лимит: {}", eventId, event.getConfirmedRequests());
        }
        RequestDto result = ReqMapper.mapToRequestDto(request);
        log.info("Запрос успешно создан с id: {} для userId: {} и eventId: {}", result.getId(), userId, eventId);
        return result;
    }

    @Transactional
    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса requestId: {} для userId: {}", requestId, userId);
        UserDto user = userClient.getUserById(userId);
        Request request = getRequest(requestId);
        if (!request.getRequesterId().equals(userId)) {
            log.warn("Пользователь userId: {} не может отменить запрос requestId: {} другого пользователя", userId, requestId);
            throw new ConflictException("Другой пользователь не может отменить запрос");
        }
        request.setStatus(RequestStatus.CANCELED);
        repository.save(request);
        EventDto event = eventClient.getEventById(request.getEventId());
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventClient.updateConfirmRequests(event.getId(), event);
        RequestDto result = ReqMapper.mapToRequestDto(request);
        log.info("Запрос requestId: {} успешно отменён для userId: {}", requestId, userId);
        return result;
    }

    @Override
    public List<RequestDto> findAllById(List<Long> ids) {
        log.info("Получение запросов по id: {}", ids);
        List<RequestDto> result = ReqMapper.mapListRequests(repository.findAllById(ids));
        log.info("Успешно получено {} запросов по указанным id", result.size());
        return result;
    }

    @Override
    public List<RequestDto> findAllByEventId(Long eventId) {
        log.info("Получение запросов по eventId: {}", eventId);
        List<RequestDto> result = ReqMapper.mapListRequests(repository.findAllByEventId(eventId));
        log.info("Успешно получено {} запросов для eventId: {}", result.size(), eventId);
        return result;
    }

    @Override
    public RequestDto updateRequest(RequestDto requestDto) {
        log.info("Обновление запроса: {}", requestDto);
        Request request = ReqMapper.mapDtoToRequest(requestDto);
        request = repository.save(request);
        RequestDto result = ReqMapper.mapToRequestDto(request);
        log.info("Запрос с id: {} успешно обновлён", result.getId());
        return result;
    }

    @Override
    public List<RequestDto> updateAllRequests(List<RequestDto> requestDtoList) {
        log.info("Обновление всех запросов, количество: {}", requestDtoList.size());
        List<RequestDto> list = new ArrayList<>(requestDtoList.size());
        for (RequestDto r : requestDtoList) {
            list.add(updateRequest(r));
        }
        log.info("Успешно обновлено {} запросов", list.size());
        return list;
    }

    private void checkRequest(Long userId, EventDto event) {
        log.debug("Проверка условий для создания запроса userId: {}, eventId: {}", userId, event.getId());
        if (!repository.findAllByRequesterIdAndEventId(userId, event.getId()).isEmpty()) {
            log.warn("Повторный запрос от userId: {} для eventId: {}", userId, event.getId());
            throw new ConflictException("нельзя добавить повторный запрос");
        }
        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Инициатор userId: {} не может создать запрос для своего события eventId: {}", userId, event.getId());
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии");
        }
        if (!event.getState().equals(EventState.PUBLISHED.toString())) {
            log.warn("Событие eventId: {} не опубликовано, статус: {}", event.getId(), event.getState());
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            log.warn("Достигнут лимит запросов для события eventId: {}, лимит: {}", event.getId(), event.getParticipantLimit());
            throw new ConflictException("у события достигнут лимит запросов на участие");
        }
    }

    private Request getRequest(Long requestId) {
        log.debug("Поиск запроса requestId: {}", requestId);
        Optional<Request> request = repository.findById(requestId);
        if (request.isEmpty()) {
            log.warn("Запрос requestId: {} не найден", requestId);
            throw new NotFoundException("Запроса с id = " + requestId.toString() + " не существует");
        }
        return request.get();
    }
}