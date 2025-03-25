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
        log.info("Получение событий для пользователя userId: {}, from: {}, size: {}", userId, from, size);
        userClient.getUserById(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<EventDto> result = repository.findByInitiatorId(userId, pageable).stream()
                .map(this::eventToDto)
                .toList();
        log.info("Успешно получено {} событий для пользователя userId: {}", result.size(), userId);
        return result;
    }

    @Override
    public EventDto getEventById(Long userId, Long id, String ip, String uri) {
        log.info("Получение события id: {} для пользователя userId: {}, IP: {}, URI: {}", id, userId, ip, uri);
        userClient.getUserById(userId);
        Optional<Event> event = repository.findByIdAndInitiatorId(id, userId);
        if (event.isEmpty()) {
            log.warn("Событие id: {} не найдено для пользователя userId: {}", id, userId);
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        EventDto result = eventToDto(event.get());
        log.info("Событие id: {} успешно получено для пользователя userId: {}", id, userId);
        return result;
    }

    @Override
    public UpdatedEventDto createEvent(Long userId, CreateEventDto eventDto) {
        log.info("Создание события для пользователя userId: {}, данные: {}", userId, eventDto);
        UserDto user = userClient.getUserById(userId);
        Category category = getCategory(eventDto.getCategory());
        Event event = EventMapper.mapCreateDtoToEvent(eventDto);

        // Установка значений по умолчанию
        setDefaultEventFields(event);

        event.setInitiatorId(userId);
        event.setCategory(category);
        event.setState(EventState.PENDING);

        Event newEvent = repository.save(event);
        UpdatedEventDto result = EventMapper.mapEventToUpdatedEventDto(newEvent, userClient.getUserById(newEvent.getInitiatorId()));
        log.info("Событие успешно создано с id: {} для пользователя userId: {}", newEvent.getId(), userId);
        return result;
    }

    @Override
    public UpdatedEventDto updateEvent(Long userId, UpdateEventDto eventDto, Long eventId) {
        log.info("Обновление события eventId: {} для пользователя userId: {}, данные: {}", eventId, userId, eventDto);
        userClient.getUserById(userId);
        Event event = getEvent(eventId);

        if (event.getState() == EventState.PUBLISHED) {
            log.warn("Нельзя изменить опубликованное событие eventId: {}", eventId);
            throw new ConflictException("Нельзя изменять опубликованное событие");
        }

        updateEventFields(eventDto, event);
        Event saved = repository.save(event);
        UpdatedEventDto result = EventMapper.mapEventToUpdatedEventDto(saved, userClient.getUserById(event.getInitiatorId()));
        log.info("Событие eventId: {} успешно обновлено для пользователя userId: {}", eventId, userId);
        return result;
    }

    // Публичные методы
    @Override
    public List<UpdatedEventDto> publicGetEvents(PublicGetEventRequestDto requestParams, Long userId) {
        log.info("Получение публичных событий с параметрами: {}", requestParams);
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
        List<UpdatedEventDto> result = EventMapper.mapToUpdatedEventDto(events);
        log.info("Успешно получено {} публичных событий", result.size());
        return result;
    }

    @Override
    public UpdatedEventDto publicGetEvent(Long id, Long userId) {
        log.info("Получение публичного события id: {}, userId: {}", id, userId);
        Event event = getEvent(id);
        validatePublishedState(event);

        collectUserActionAndUpdateRating(id, userId, event);

        UserDto initiator = userClient.getUserById(event.getInitiatorId());
        collectUserActionAndUpdateRating(id, userId, event);
        UpdatedEventDto result = EventMapper.mapEventToUpdatedEventDto(event, initiator);
        log.info("Публичное событие id: {} успешно получено", id);
        return result;
    }

    @Override
    public EventDto publicGetEvent(Long eventId) {
        log.info("Получение публичного события по id: {}", eventId);
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Событие id: {} не опубликовано", eventId);
            return null;
        }
        UserDto initiator = userClient.getUserById(event.getInitiatorId());
        EventDto result = EventMapper.mapEventToEventDto(event, initiator);
        log.info("Публичное событие id: {} успешно получено", eventId);
        return result;
    }

    // Методы для работы с запросами
    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение запросов для события eventId: {} пользователя userId: {}", eventId, userId);
        userClient.getUserById(userId);
        getEvent(eventId);
        List<RequestDto> result = requestClient.getRequestsByEventId(eventId);
        log.info("Успешно получено {} запросов для события eventId: {}", result.size(), eventId);
        return result;
    }

    @Override
    public EventRequestStatusUpdateResult changeStatusEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса запросов для события eventId: {} пользователя userId: {}, данные: {}", eventId, userId, request);
        userClient.getUserById(userId);
        Event event = getEvent(eventId);
        EventRequestStatusUpdateResult response = new EventRequestStatusUpdateResult();
        List<RequestDto> requests = requestClient.findAllById(request.getRequestIds());

        if (request.getStatus().equals(RequestStatus.REJECTED)) {
            handleRequestRejection(requests, response);
        } else {
            handleRequestConfirmation(event, requests, response);
        }
        log.info("Статус запросов для события eventId: {} успешно изменён", eventId);
        return response;
    }

    // Админские методы
    @Override
    public List<UpdatedEventDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        log.info("Получение событий администратором с параметрами: {}", requestParams);
        List<Event> events = repository.findEventsByAdmin(
                requestParams.getUsers(),
                requestParams.getStates(),
                requestParams.getCategories(),
                requestParams.getRangeStart(),
                requestParams.getRangeEnd(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(), requestParams.getSize())
        );
        List<UpdatedEventDto> result = EventMapper.mapToUpdatedEventDto(events);
        log.info("Администратор успешно получил {} событий", result.size());
        return result;
    }

    @Override
    public UpdatedEventDto adminChangeEvent(Long eventId, UpdateEventDto eventDto) {
        log.info("Администратор изменяет событие eventId: {}, данные: {}", eventId, eventDto);
        Event event = getEvent(eventId);
        checkEventForUpdate(event, eventDto.getStateAction());

        Event updatedEvent = repository.save(prepareEventForUpdate(event, eventDto));
        UserDto initiator = userClient.getUserById(event.getInitiatorId());
        UpdatedEventDto result = EventMapper.mapEventToUpdatedEventDto(updatedEvent, initiator);
        log.info("Событие eventId: {} успешно обновлено администратором", eventId);
        return result;
    }

    // Дополнительные методы
    @Override
    public EventDto getEventByInitiator(Long userId) {
        log.info("Получение события по инициатору с ID: {}", userId);
        Event result = repository.findByInitiatorId(userId);
        UserDto initiator = userClient.getUserById(userId);
        EventDto eventDto = EventMapper.mapEventToEventDto(result, initiator);
        log.info("Событие для инициатора с ID {} успешно получено", userId);
        return eventDto;
    }

    public EventDto updateConfirmRequests(EventDto eventDto) {
        log.info("Обновление подтверждения запросов для события: {}", eventDto);
        Event event = EventMapper.mapToEvent(eventDto);
        UserDto user = userClient.getUserById(event.getInitiatorId());
        Event savedEvent = repository.save(event);
        EventDto result = EventMapper.mapEventToEventDto(savedEvent, user);
        log.info("Подтверждение запросов для события id: {} успешно обновлено", savedEvent.getId());
        return result;
    }

    @Override
    public List<RecommendationDto> getRecommendations(Long limit, Long userId) {
        log.info("Получение рекомендаций с лимитом: {}, пользователь: {}", limit, userId);
        try {
            List<RecommendationDto> result = recommendationsClient.getRecommendationsForUser(
                            (userId), limit)
                    .map(x -> RecommendationDto.builder()
                            .eventId(x.getEventId())
                            .score(x.getScore())
                            .build())
                    .toList();
            log.info("Успешно получено {} рекомендаций", result.size());
            return result;
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void saveLike(Long eventId, Long userId) {
        log.info("Сохранение лайка для события eventId: {}, пользователь: {}", eventId, userId);
        Optional<Event> eventOptional = repository.findById(eventId);

        validateLikeConditions(eventOptional, eventId, userId);

        try {
            collectorClient.collectUserAction(UserActionProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .build());
            log.info("Лайк для события eventId: {} успешно сохранён", eventId);
        } catch (Exception e) {
            log.error("Ошибка при сохранении лайка для события eventId: {}: {}", eventId, e.getMessage(), e);
        }
    }

    // Вспомогательные методы
    private EventDto eventToDto(Event event) {
        log.debug("Преобразование события id: {} в DTO", event.getId());
        return EventMapper.mapEventToEventDto(event, userClient.getUserById(event.getInitiatorId()));
    }

    private Event getEvent(Long eventId) {
        log.debug("Поиск события id: {}", eventId);
        return repository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие id: {} не найдено", eventId);
                    return new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
                });
    }

    private void setDefaultEventFields(Event event) {
        log.debug("Установка значений по умолчанию для события");
        event.setPaid(event.getPaid() != null ? event.getPaid() : false);
        event.setParticipantLimit(event.getParticipantLimit() != null ? event.getParticipantLimit() : 0);
        event.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.warn("Некорректный диапазон дат: start={} позже end={}", start, end);
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
    }

    private void validatePublishedState(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Событие id: {} не опубликовано, текущий статус: {}", event.getId(), event.getState());
            throw new NotFoundException("Событие не найдено или не опубликовано");
        }
    }

    private void collectUserActionAndUpdateRating(Long eventId, long userId, Event event) {
        log.debug("Сбор действий пользователя и обновление рейтинга для события id: {}", eventId);
        UserActionProto userActionProto = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .build();
        try {
            collectorClient.collectUserAction(userActionProto);
            Stream<RecommendedEventProto> rating = recommendationsClient.getInteractionsCount(List.of(eventId));
            rating.findFirst().ifPresent(recommendedEvent -> {
                event.setRating(recommendedEvent.getScore());
                repository.save(event);
                log.debug("Рейтинг события id: {} обновлён: {}", eventId, recommendedEvent.getScore());
            });
        } catch (Exception e) {
            log.error("Ошибка при сборе действий пользователя или обновлении рейтинга для события id: {}: {}", eventId, e.getMessage(), e);
        }
    }

    private void handleRequestRejection(List<RequestDto> requests, EventRequestStatusUpdateResult response) {
        log.debug("Обработка отклонения запросов, количество: {}", requests.size());
        checkRequestsStatus(requests);
        List<RequestDto> rejectedRequests = requests.stream()
                .peek(req -> req.setStatus(RequestStatus.REJECTED))
                .collect(Collectors.toList());
        requestClient.updateAllRequest(rejectedRequests);
        response.setRejectedRequests(rejectedRequests);
        log.debug("Успешно отклонено {} запросов", rejectedRequests.size());
    }

    private void handleRequestConfirmation(Event event, List<RequestDto> requests, EventRequestStatusUpdateResult response) {
        log.debug("Обработка подтверждения запросов для события id: {}, количество: {}", event.getId(), requests.size());
        if (requests.size() + event.getConfirmedRequests() > event.getParticipantLimit()) {
            log.warn("Превышен лимит участников для события id: {}, текущий: {}, новый: {}",
                    event.getId(), event.getConfirmedRequests(), requests.size());
            throw new ConflictException("Превышен лимит заявок");
        }
        List<RequestDto> confirmedRequests = requests.stream()
                .peek(req -> req.setStatus(RequestStatus.CONFIRMED))
                .collect(Collectors.toList());
        requestClient.updateAllRequest(confirmedRequests);
        event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
        repository.save(event);
        response.setConfirmedRequests(confirmedRequests);
        log.debug("Успешно подтверждено {} запросов для события id: {}", confirmedRequests.size(), event.getId());
    }

    private void checkEventForUpdate(Event event, StateAction action) {
        log.debug("Проверка события id: {} перед обновлением, действие: {}", event.getId(), action);
        checkEventDate(event.getEventDate());
        if (action == null) return;
        if (action.equals(StateAction.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
            log.warn("Нельзя опубликовать событие id: {} в статусе: {}", event.getId(), event.getState());
            throw new ConflictException("Опубликовать можно только событие в статусе PENDING, текущий статус: " + event.getState());
        }
        if (action.equals(StateAction.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Нельзя отменить опубликованное событие id: {}, статус: {}", event.getId(), event.getState());
            throw new ConflictException("Отменить можно только неопубликованное событие, текущий статус: " + event.getState());
        }
    }

    private Event prepareEventForUpdate(Event event, UpdateEventDto updateEventDto) {
        log.debug("Подготовка события id: {} к обновлению", event.getId());
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
            log.warn("Некорректная дата события: {}, должна быть не ранее чем через час", dateTime);
            throw new ConflictException("Дата начала события должна быть не ранее чем через час: " + dateTime);
        }
    }

    private Category getCategory(Long categoryId) {
        log.debug("Поиск категории id: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Категория id: {} не найдена", categoryId);
                    return new NotFoundException("Категория не найдена");
                });
    }

    private void updateEventFields(UpdateEventDto eventDto, Event foundEvent) {
        log.debug("Обновление полей события id: {}", foundEvent.getId());
        if (eventDto.getCategory() != null) foundEvent.setCategory(getCategory(eventDto.getCategory()));
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank())
            foundEvent.setAnnotation(eventDto.getAnnotation());
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank())
            foundEvent.setDescription(eventDto.getDescription());
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Некорректная дата события id: {}: {}", foundEvent.getId(), eventDto.getEventDate());
                throw new ConflictException("Дата начала события не может быть раньше чем через 2 часа");
            }
            foundEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) foundEvent.setPaid(eventDto.getPaid());
        if (eventDto.getParticipantLimit() != null) {
            if (eventDto.getParticipantLimit() < 0) {
                log.warn("Некорректный лимит участников для события id: {}: {}", foundEvent.getId(), eventDto.getParticipantLimit());
                throw new ValidationException("Лимит участников не может быть отрицательным");
            }
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
            log.warn("Нельзя поставить лайк событию id: {} пользователем userId: {} - условия не выполнены", eventId, userId);
            throw new ValidationException("Нельзя поставить лайк, не посетив мероприятие");
        }
    }

    private List<String> getListOfUri(List<Event> events, String uri) {
        log.debug("Генерация URI для {} событий", events.size());
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
            log.warn("Нельзя отменить уже подтверждённые запросы, количество: {}", requests.size());
            throw new ConflictException("Нельзя отменить уже принятую заявку");
        }
    }
}