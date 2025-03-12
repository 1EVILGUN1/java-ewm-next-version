package ewm.request.service;

import ewm.dto.request.RequestDto;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.event.repository.EventRepository;
import ewm.model.event.Event;
import ewm.model.event.EventState;
import ewm.model.request.Request;
import ewm.model.request.RequestStatus;
import ewm.model.user.User;
import ewm.request.mapper.RequestMapper;
import ewm.request.repository.RequestRepository;
import ewm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private static final int DEFAULT_PARTICIPANT_LIMIT = 0;
    private static final int INCREMENT_CONFIRMED_REQUESTS = 1;
    private static final int DECREMENT_CONFIRMED_REQUESTS = -1;

    private final RequestRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public RequestDto create(Long userId, Long eventId) {
        log.info("Создание запроса на участие от пользователя с ID {} для события с ID {}", userId, eventId);
        Event event = getEvent(eventId);
        User user = getUser(userId);
        checkRequest(userId, event);
        Request request = Request.builder()
                .requester(user)
                .created(LocalDateTime.now())
                .status(!event.getRequestModeration()
                        || event.getParticipantLimit() == DEFAULT_PARTICIPANT_LIMIT
                        ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .event(event)
                .build();
        request = repository.save(request);
        if (!event.getRequestModeration() || event.getParticipantLimit() == DEFAULT_PARTICIPANT_LIMIT) {
            event.setConfirmedRequests(event.getConfirmedRequests() + INCREMENT_CONFIRMED_REQUESTS);
            eventRepository.save(event);
            log.debug("Увеличено количество подтвержденных запросов для события с ID {}: {}", eventId, event.getConfirmedRequests());
        }
        RequestDto result = RequestMapper.INSTANCE.mapToRequestDto(request);
        log.info("Запрос на участие успешно создан для пользователя с ID {} и события с ID {}", userId, eventId);
        return result;
    }

    @Transactional
    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с ID {} пользователем с ID {}", requestId, userId);
        getUser(userId);
        Request request = getRequest(requestId);
        if (!request.getRequester().getId().equals(userId)) {
            log.error("Пользователь с ID {} пытается отменить чужой запрос с ID {}", userId, requestId);
            throw new ConflictException("Другой пользователь не может отменить запрос");
        }
        request.setStatus(RequestStatus.CANCELED);
        repository.save(request);
        Event event = getEvent(request.getEvent().getId());
        event.setConfirmedRequests(event.getConfirmedRequests() + DECREMENT_CONFIRMED_REQUESTS);
        eventRepository.save(event);
        log.debug("Уменьшено количество подтвержденных запросов для события с ID {}: {}", event.getId(), event.getConfirmedRequests());
        RequestDto result = RequestMapper.INSTANCE.mapToRequestDto(request);
        log.info("Запрос с ID {} успешно отменен пользователем с ID {}", requestId, userId);
        return result;
    }

    @Override
    public List<RequestDto> getRequests(Long userId) {
        log.info("Получение списка запросов на участие для пользователя с ID {}", userId);
        getUser(userId);
        List<RequestDto> result = RequestMapper.INSTANCE.mapListRequests(repository.findAllByRequester_Id(userId));
        log.info("Получено {} запросов на участие для пользователя с ID {}", result.size(), userId);
        return result;
    }

    private void checkRequest(Long userId, Event event) {
        if (!repository.findAllByRequester_IdAndEvent_id(userId, event.getId()).isEmpty()) {
            log.error("Повторный запрос от пользователя с ID {} для события с ID {}", userId, event.getId());
            throw new ConflictException("Нельзя добавить повторный запрос");
        }
        if (event.getInitiator().getId().equals(userId)) {
            log.error("Инициатор события с ID {} пытается создать запрос на участие", userId);
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Попытка участия в неопубликованном событии с ID {}", event.getId());
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (event.getParticipantLimit() != DEFAULT_PARTICIPANT_LIMIT &&
            event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            log.error("Достигнут лимит участников для события с ID {}", event.getId());
            throw new ConflictException("У события достигнут лимит запросов на участие");
        }
    }

    private Event getEvent(Long eventId) {
        log.debug("Поиск события с ID: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Событие с ID {} не найдено", eventId);
                    return new NotFoundException("События с id = " + eventId + " не существует");
                });
    }

    private User getUser(Long userId) {
        log.debug("Поиск пользователя с ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователя с id = " + userId + " не существует");
                });
    }

    private Request getRequest(Long requestId) {
        log.debug("Поиск запроса с ID: {}", requestId);
        return repository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Запрос с ID {} не найден", requestId);
                    return new NotFoundException("Запроса с id = " + requestId + " не существует");
                });
    }
}