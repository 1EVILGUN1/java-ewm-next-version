package ewm.event.service;

import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.client.CollectorClient;
import ewm.client.RecommendationsClient;
import ewm.client.RequestOperations;
import ewm.client.UserClient;
import ewm.dto.event.*;
import ewm.dto.request.RequestDto;
import ewm.dto.user.UserDto;
import ewm.enums.EventState;
import ewm.enums.RequestStatus;
import ewm.enums.StateAction;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.error.exception.ValidationException;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.mapper.EventMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.action.UserActionProto;
import ru.practicum.ewm.grpc.stats.recomendations.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    // Константы
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found";
    private static final String USER_ID_HEADER = "X-EWM-USER-ID";

    // Зависимости
    private final EventRepository repository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final RequestOperations requestClient;
    private final CollectorClient collectorClient;
    private final RecommendationsClient recommendationsClient;

    // Методы для пользователей
    @Override
    public List<EventDto> getEvents(Long userId, Integer from, Integer size) {
        userClient.getUserById(userId);
        Pageable pageable = PageRequest.of(from, size);
        return repository.findByInitiatorId(userId, pageable).stream()
                .map(this::eventToDto)
                .toList();
    }

    @Override
    public EventDto getEventById(Long userId, Long id, String ip, String uri) {
        userClient.getUserById(userId);
        Optional<Event> event = repository.findByIdAndInitiatorId(id, userId);
        if (event.isEmpty()) {
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        return eventToDto(event.get());
    }

    @Override
    public UpdatedEventDto createEvent(Long userId, CreateEventDto eventDto) {
        UserDto user = userClient.getUserById(userId);
        Category category = getCategory(eventDto.getCategory());
        Event event = EventMapper.mapCreateDtoToEvent(eventDto);

        // Установка значений по умолчанию
        setDefaultEventFields(event);

        event.setInitiatorId(userId);
        event.setCategory(category);
        event.setState(EventState.PENDING);

        Event newEvent = repository.save(event);
        return EventMapper.mapEventToUpdatedEventDto(newEvent, userClient.getUserById(event.getInitiatorId()));
    }

    @Override
    public UpdatedEventDto updateEvent(Long userId, UpdateEventDto eventDto, Long eventId) {
        userClient.getUserById(userId);
        Event event = getEvent(eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять опубликованное событие");
        }

        updateEventFields(eventDto, event);
        Event saved = repository.save(event);
        return EventMapper.mapEventToUpdatedEventDto(saved, userClient.getUserById(event.getInitiatorId()));
    }

    // Публичные методы
    @Override
    public List<UpdatedEventDto> publicGetEvents(PublicGetEventRequestDto requestParams, HttpServletRequest request) {
        LocalDateTime start = requestParams.getRangeStart() != null ? requestParams.getRangeStart() : LocalDateTime.now();
        LocalDateTime end = requestParams.getRangeEnd() != null ? requestParams.getRangeEnd() : LocalDateTime.now().plusYears(10);

        validateDateRange(start, end);

        List<Event> events = repository.findEventsPublic(
                requestParams.getText(),
                requestParams.getCategories(),
                requestParams.getPaid(),
                start,
                end,
                EventState.PUBLISHED,
                requestParams.getOnlyAvailable(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(), requestParams.getSize())
        );

        return EventMapper.mapToUpdatedEventDto(events);
    }

    @Override
    public UpdatedEventDto publicGetEvent(Long id, HttpServletRequest request) {
        Event event = getEvent(id);
        validatePublishedState(event);

        collectUserActionAndUpdateRating(id, request, event);

        UserDto initiator = userClient.getUserById(event.getInitiatorId());
        return EventMapper.mapEventToUpdatedEventDto(event, initiator);
    }

    @Override
    public EventDto publicGetEvent(Long eventId) {
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            return null;
        }
        UserDto initiator = userClient.getUserById(event.getInitiatorId());
        return EventMapper.mapEventToEventDto(event, initiator);
    }

    // Методы для работы с запросами
    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        userClient.getUserById(userId);
        getEvent(eventId);
        return requestClient.getRequestsByEventId(eventId);
    }

    @Override
    public EventRequestStatusUpdateResult changeStatusEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        userClient.getUserById(userId);
        Event event = getEvent(eventId);
        EventRequestStatusUpdateResult response = new EventRequestStatusUpdateResult();
        List<RequestDto> requests = requestClient.findAllById(request.getRequestIds());

        if (request.getStatus().equals(RequestStatus.REJECTED)) {
            handleRequestRejection(requests, response);
        } else {
            handleRequestConfirmation(event, requests, response);
        }
        return response;
    }

    // Админские методы
    @Override
    public List<UpdatedEventDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        List<Event> events = repository.findEventsByAdmin(
                requestParams.getUsers(),
                requestParams.getStates(),
                requestParams.getCategories(),
                requestParams.getRangeStart(),
                requestParams.getRangeEnd(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(), requestParams.getSize())
        );
        return EventMapper.mapToUpdatedEventDto(events);
    }

    @Override
    public UpdatedEventDto adminChangeEvent(Long eventId, UpdateEventDto eventDto) {
        Event event = getEvent(eventId);
        checkEventForUpdate(event, eventDto.getStateAction());

        Event updatedEvent = repository.save(prepareEventForUpdate(event, eventDto));
        UserDto initiator = userClient.getUserById(event.getInitiatorId());
        return EventMapper.mapEventToUpdatedEventDto(updatedEvent, initiator);
    }

    // Дополнительные методы
    @Override
    public EventDto getEventByInitiator(Long userId) {
        log.info("Получение события по инициатору с ID: {}", userId);
        Event result = repository.findByInitiatorId(userId);
        UserDto initiator = userClient.getUserById(userId);
        log.info("Событие для инициатора с ID {} успешно получено", userId);
        return EventMapper.mapEventToEventDto(result, initiator);
    }

    public EventDto updateConfirmRequests(EventDto eventDto) {
        Event event = EventMapper.mapToEvent(eventDto);
        UserDto user = userClient.getUserById(event.getInitiatorId());
        return EventMapper.mapEventToEventDto(repository.save(event), user);
    }

    public List<RecommendationDto> getRecommendations(Long limit, HttpServletRequest request) {
        try {
            return recommendationsClient.getRecommendationsForUser(Long.parseLong(request.getHeader(USER_ID_HEADER)), limit)
                    .map(x -> RecommendationDto.builder()
                            .eventId(x.getEventId())
                            .score(x.getScore())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveLike(Long eventId, HttpServletRequest request) {
        Optional<Event> eventOptional = repository.findById(eventId);
        Long userId = Long.parseLong(request.getHeader(USER_ID_HEADER));

        validateLikeConditions(eventOptional, eventId, userId);

        try {
            collectorClient.collectUserAction(UserActionProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при сохранении лайка: {}", e.getMessage());
        }
    }

    // Вспомогательные методы
    private EventDto eventToDto(Event event) {
        return EventMapper.mapEventToEventDto(event, userClient.getUserById(event.getInitiatorId()));
    }

    private Event getEvent(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_MESSAGE));
    }

    private void setDefaultEventFields(Event event) {
        event.setPaid(event.getPaid() != null ? event.getPaid() : false);
        event.setParticipantLimit(event.getParticipantLimit() != null ? event.getParticipantLimit() : 0);
        event.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
    }

    private void validatePublishedState(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие не найдено или не опубликовано");
        }
    }

    private void collectUserActionAndUpdateRating(Long eventId, HttpServletRequest request, Event event) {
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(request.getIntHeader(USER_ID_HEADER))
                .build();
        try {
            collectorClient.collectUserAction(userActionProto);
            Stream<RecommendedEventProto> rating = recommendationsClient.getInteractionsCount(List.of(eventId));
            rating.findFirst().ifPresent(recommendedEvent -> {
                event.setRating(recommendedEvent.getScore());
                repository.save(event);
            });
        } catch (Exception e) {
            log.error("Ошибка при сборе действий пользователя или обновлении рейтинга: {}", e.getMessage());
        }
    }

    private void handleRequestRejection(List<RequestDto> requests, EventRequestStatusUpdateResult response) {
        checkRequestsStatus(requests);
        List<RequestDto> rejectedRequests = requests.stream()
                .peek(req -> req.setStatus(RequestStatus.REJECTED))
                .collect(Collectors.toList());
        requestClient.updateAllRequest(rejectedRequests);
        response.setRejectedRequests(rejectedRequests);
    }

    private void handleRequestConfirmation(Event event, List<RequestDto> requests, EventRequestStatusUpdateResult response) {
        if (requests.size() + event.getConfirmedRequests() > event.getParticipantLimit()) {
            throw new ConflictException("Превышен лимит заявок");
        }
        List<RequestDto> confirmedRequests = requests.stream()
                .peek(req -> req.setStatus(RequestStatus.CONFIRMED))
                .collect(Collectors.toList());
        requestClient.updateAllRequest(confirmedRequests);
        event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
        repository.save(event);
        response.setConfirmedRequests(confirmedRequests);
    }

    private void checkEventForUpdate(Event event, StateAction action) {
        checkEventDate(event.getEventDate());
        if (action == null) return;
        if (action.equals(StateAction.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
            throw new ConflictException("Опубликовать можно только событие в статусе PENDING, текущий статус: " + event.getState());
        }
        if (action.equals(StateAction.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Отменить можно только неопубликованное событие, текущий статус: " + event.getState());
        }
    }

    private Event prepareEventForUpdate(Event event, UpdateEventDto updateEventDto) {
        if (updateEventDto.getAnnotation() != null) event.setAnnotation(updateEventDto.getAnnotation());
        if (updateEventDto.getDescription() != null) event.setDescription(updateEventDto.getDescription());
        if (updateEventDto.getEventDate() != null) {
            checkEventDate(updateEventDto.getEventDate());
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getPaid() != null) event.setPaid(updateEventDto.getPaid());
        if (updateEventDto.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        if (updateEventDto.getTitle() != null) event.setTitle(updateEventDto.getTitle());
        if (updateEventDto.getStateAction() != null) {
            switch (updateEventDto.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case CANCEL_REVIEW, REJECT_EVENT -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
        return event;
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата начала события должна быть не ранее чем через час: " + dateTime);
        }
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

    private void updateEventFields(UpdateEventDto eventDto, Event foundEvent) {
        if (eventDto.getCategory() != null) foundEvent.setCategory(getCategory(eventDto.getCategory()));
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank())
            foundEvent.setAnnotation(eventDto.getAnnotation());
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank())
            foundEvent.setDescription(eventDto.getDescription());
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Дата начала события не может быть раньше чем через 2 часа");
            }
            foundEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) foundEvent.setPaid(eventDto.getPaid());
        if (eventDto.getParticipantLimit() != null) {
            if (eventDto.getParticipantLimit() < 0)
                throw new ValidationException("Лимит участников не может быть отрицательным");
            foundEvent.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) foundEvent.setRequestModeration(eventDto.getRequestModeration());
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) foundEvent.setTitle(eventDto.getTitle());
        if (eventDto.getLocation() != null) {
            if (eventDto.getLocation().getLat() != null) foundEvent.setLat(eventDto.getLocation().getLat());
            if (eventDto.getLocation().getLon() != null) foundEvent.setLon(eventDto.getLocation().getLon());
        }
        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case CANCEL_REVIEW -> foundEvent.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> foundEvent.setState(EventState.PUBLISHED);
                case SEND_TO_REVIEW -> foundEvent.setState(EventState.PENDING);
            }
        }
    }

    private void validateLikeConditions(Optional<Event> eventOptional, Long eventId, Long userId) {
        if (eventOptional.isEmpty() || eventOptional.get().getEventDate().isAfter(LocalDateTime.now()) ||
            requestClient.getRequestsByEventId(eventId).stream().noneMatch(x -> x.getRequester().equals(userId))) {
            throw new ValidationException("Нельзя поставить лайк, не посетив мероприятие");
        }
    }

    private List<String> getListOfUri(List<Event> events, String uri) {
        return events.stream()
                .map(Event::getId)
                .map(id -> getUriForEvent(uri, id))
                .collect(Collectors.toList());
    }

    private String getUriForEvent(String uri, Long eventId) {
        return uri + "/" + eventId;
    }

    private void checkRequestsStatus(List<RequestDto> requests) {
        if (requests.stream().anyMatch(req -> req.getStatus().equals(RequestStatus.CONFIRMED))) {
            throw new ConflictException("Нельзя отменить уже принятую заявку");
        }
    }
}