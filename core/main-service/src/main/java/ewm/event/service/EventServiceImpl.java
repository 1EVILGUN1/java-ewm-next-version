package ewm.event.service;

import ewm.category.repository.CategoryRepository;
import ewm.dto.event.*;
import ewm.dto.request.RequestDto;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.error.exception.ValidationException;
import ewm.event.mapper.EventMapper;
import ewm.event.repository.EventRepository;
import ewm.model.category.Category;
import ewm.model.event.Event;
import ewm.model.event.EventState;
import ewm.model.request.Request;
import ewm.model.request.RequestStatus;
import ewm.model.request.StateAction;
import ewm.model.user.User;
import ewm.request.mapper.RequestMapper;
import ewm.request.repository.RequestRepository;
import ewm.statistic.StatisticsService;
import ewm.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found";
    private static final int DEFAULT_PARTICIPANT_LIMIT = 0;
    private static final boolean DEFAULT_PAID_STATUS = false;
    private static final boolean DEFAULT_REQUEST_MODERATION = true;
    private static final int MINIMUM_HOURS_BEFORE_EVENT_ADMIN = 1;
    private static final int MINIMUM_HOURS_BEFORE_EVENT_USER = 2;
    private static final long YEARS_TO_ADD_FOR_DEFAULT_END_DATE = 10L;

    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticsService statisticsService;
    private final RequestRepository requestRepository;

    @Override
    public EventDto create(Long userId, CreateEventDto eventDto) {
        log.info("Создание нового события пользователем с ID {}: {}", userId, eventDto);
        User user = getUser(userId);
        Category category = getCategory(eventDto.getCategory());
        Event event = EventMapper.mapCreateDtoToEvent(eventDto);
        if (event.getPaid() == null) {
            event.setPaid(DEFAULT_PAID_STATUS);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(DEFAULT_PARTICIPANT_LIMIT);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(DEFAULT_REQUEST_MODERATION);
        }

        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        Event newEvent = repository.save(event);
        log.info("Событие успешно создано: {}", newEvent);
        return eventToDto(newEvent);
    }

    @Override
    public EventDto update(Long userId, UpdateEventDto eventDto, Long eventId) {
        log.info("Обновление события с ID {} пользователем с ID {}: {}", eventId, userId, eventDto);
        getUser(userId);
        Optional<Event> eventOptional = repository.findById(eventId);
        if (eventOptional.isEmpty()) {
            log.error("Событие с ID {} не найдено", eventId);
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        Event foundEvent = eventOptional.get();
        if (foundEvent.getState() == EventState.PUBLISHED) {
            log.error("Попытка изменить опубликованное событие с ID {}", eventId);
            throw new ConflictException("Нельзя изменять сообщение, которое опубликовано");
        }
        updateEventFields(eventDto, foundEvent);
        Event saved = repository.save(foundEvent);
        log.info("Событие с ID {} успешно обновлено: {}", eventId, saved);
        return eventToDto(saved);
    }

    @Override
    public List<EventDto> getEvents(Long userId, Integer from, Integer size) {
        log.info("Получение событий пользователя с ID {}, from = {}, size = {}", userId, from, size);
        getUser(userId);
        Pageable pageable = PageRequest.of(from, size);
        List<EventDto> result = repository.findByInitiatorId(userId, pageable).stream()
                .map(this::eventToDto)
                .toList();
        log.info("Получено {} событий для пользователя с ID {}", result.size(), userId);
        return result;
    }

    @Override
    public EventDto getEventById(Long userId, Long id, String ip, String uri) {
        log.info("Получение события с ID {} для пользователя с ID {}", id, userId);
        getUser(userId);
        Optional<Event> event = repository.findByIdAndInitiatorId(id, userId);
        if (event.isEmpty()) {
            log.error("Событие с ID {} не найдено для пользователя с ID {}", id, userId);
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        EventDto result = eventToDto(event.get());
        log.info("Событие с ID {} успешно получено", id);
        return result;
    }

    @Override
    public List<EventDto> publicGetEvents(PublicGetEventRequestDto requestParams,
                                          HttpServletRequest request) {
        log.info("Публичное получение событий с параметрами: {}", requestParams);
        LocalDateTime start = (requestParams.getRangeStart() == null) ?
                LocalDateTime.now() : requestParams.getRangeStart();
        LocalDateTime end = (requestParams.getRangeEnd() == null) ?
                LocalDateTime.now().plusYears(YEARS_TO_ADD_FOR_DEFAULT_END_DATE) : requestParams.getRangeEnd();

        if (start.isAfter(end)) {
            log.error("Дата начала {} позже даты окончания {}", start, end);
            throw new ValidationException("Дата окончания должна быть больше даты старта.");
        }
        List<Event> events = repository.findEventsPublic(
                requestParams.getText(),
                requestParams.getCategories(),
                requestParams.getPaid(),
                start,
                end,
                EventState.PUBLISHED,
                requestParams.getOnlyAvailable(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(),
                        requestParams.getSize())
        );

        statisticsService.save(request);
        if (!events.isEmpty()) {
            LocalDateTime oldestEventPublishedOn = events.stream()
                    .min(Comparator.comparing(Event::getPublishedOn)).map(Event::getPublishedOn).stream()
                    .findFirst().orElseThrow();
            List<String> uris = getListOfUri(events, request.getRequestURI());

            Map<Long, Long> views = statisticsService.getStats(oldestEventPublishedOn, LocalDateTime.now(), uris);
            events.forEach(event -> event.setViews(views.get(event.getId())));
            events = repository.saveAll(events);
        }
        List<EventDto> result = EventMapper.mapToEventDto(events);
        log.info("Публично получено {} событий", result.size());
        return result;
    }

    @Override
    public EventDto publicGetEvent(Long id, HttpServletRequest request) {
        log.info("Публичное получение события с ID: {}", id);
        Event event = getEvent(id);
        if (event.getState() != EventState.PUBLISHED) {
            log.error("Событие с ID {} не опубликовано", id);
            throw new NotFoundException("Событие не найдено");
        }
        statisticsService.save(request);
        Long views = statisticsService.getStats(
                event.getPublishedOn(), LocalDateTime.now(), List.of(request.getRequestURI())).get(id);
        event.setViews(views);
        event = repository.save(event);
        EventDto result = EventMapper.mapEventToEventDto(event);
        log.info("Событие с ID {} успешно получено публично", id);
        return result;
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение запросов на участие в событии с ID {} для пользователя с ID {}", eventId, userId);
        getUser(userId);
        getEvent(eventId);
        List<RequestDto> result = RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllByEvent_id(eventId));
        log.info("Получено {} запросов для события с ID {}", result.size(), eventId);
        return result;
    }

    @Override
    public EventRequestStatusUpdateResult changeStatusEventRequests(Long userId, Long eventId,
                                                                    EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса запросов для события с ID {} пользователем с ID {}: {}", eventId, userId, request);
        getUser(userId);
        Event event = getEvent(eventId);
        EventRequestStatusUpdateResult response = new EventRequestStatusUpdateResult();
        List<Request> requests = requestRepository.findAllById(request.getRequestIds());

        if (request.getStatus().equals(RequestStatus.REJECTED)) {
            checkRequestsStatus(requests);
            requests.forEach(tmpReq -> changeStatus(tmpReq, RequestStatus.REJECTED));
            requestRepository.saveAll(requests);
            response.setRejectedRequests(RequestMapper.INSTANCE.mapListRequests(requests));
            log.info("Отклонено {} запросов для события с ID {}", requests.size(), eventId);
        } else {
            if (requests.size() + event.getConfirmedRequests() > event.getParticipantLimit()) {
                log.error("Превышен лимит участников для события с ID {}", eventId);
                throw new ConflictException("Превышен лимит заявок");
            }
            requests.forEach(tmpReq -> changeStatus(tmpReq, RequestStatus.CONFIRMED));
            requestRepository.saveAll(requests);
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            repository.save(event);
            response.setConfirmedRequests(RequestMapper.INSTANCE.mapListRequests(requests));
            log.info("Подтверждено {} запросов для события с ID {}", requests.size(), eventId);
        }
        return response;
    }

    @Override
    public List<EventDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        log.info("Админ: получение событий с параметрами: {}", requestParams);
        List<Event> events = repository.findEventsByAdmin(
                requestParams.getUsers(),
                requestParams.getStates(),
                requestParams.getCategories(),
                requestParams.getRangeStart(),
                requestParams.getRangeEnd(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(),
                        requestParams.getSize())
        );
        List<EventDto> result = EventMapper.mapToEventDto(events);
        log.info("Админ: получено {} событий", result.size());
        return result;
    }

    @Override
    public EventDto adminChangeEvent(Long eventId, UpdateEventDto eventDto) {
        log.info("Админ: обновление события с ID {}: {}", eventId, eventDto);
        Event event = getEvent(eventId);
        checkEventForUpdate(event, eventDto.getStateAction());
        Event updatedEvent = repository.save(prepareEventForUpdate(event, eventDto));
        EventDto result = EventMapper.mapEventToEventDto(updatedEvent);
        log.info("Админ: событие с ID {} успешно обновлено", eventId);
        return result;
    }

    @Override
    public Event getEventByInitiator(Long userId) {
        log.info("Получение события по инициатору с ID: {}", userId);
        Event result = repository.findByInitiatorId(userId);
        log.info("Событие для инициатора с ID {} успешно получено", userId);
        return result;
    }

    private EventDto eventToDto(Event event) {
        log.debug("Преобразование события в DTO: {}", event);
        return EventMapper.mapEventToEventDto(event);
    }

    private Event getEvent(Long eventId) {
        log.debug("Поиск события с ID: {}", eventId);
        return repository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Событие с ID {} не найдено", eventId);
                    return new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
                });
    }

    private void checkEventForUpdate(Event event, StateAction action) {
        checkEventDate(event.getEventDate());
        if (action == null) return;
        if (action.equals(StateAction.PUBLISH_EVENT)
            && !event.getState().equals(EventState.PENDING)) {
            log.error("Попытка опубликовать событие с ID {} не в статусе PENDING", event.getId());
            throw new ConflictException("Опубликовать событие можно в статусе PENDING, а статус = " + event.getState());
        }
        if (action.equals(StateAction.REJECT_EVENT)
            && event.getState().equals(EventState.PUBLISHED)) {
            log.error("Попытка отменить опубликованное событие с ID {}", event.getId());
            throw new ConflictException("Отменить событие можно только в статусе PUBLISHED, а статус = " + event.getState());
        }
    }

    private Event prepareEventForUpdate(Event event, UpdateEventDto updateEventDto) {
        log.debug("Подготовка события с ID {} к обновлению: {}", event.getId(), updateEventDto);
        if (updateEventDto.getAnnotation() != null)
            event.setAnnotation(updateEventDto.getAnnotation());
        if (updateEventDto.getDescription() != null)
            event.setDescription(updateEventDto.getDescription());
        if (updateEventDto.getEventDate() != null) {
            checkEventDate(updateEventDto.getEventDate());
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getPaid() != null)
            event.setPaid(updateEventDto.getPaid());
        if (updateEventDto.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        if (updateEventDto.getTitle() != null)
            event.setTitle(updateEventDto.getTitle());
        if (updateEventDto.getStateAction() != null) {
            switch (updateEventDto.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case CANCEL_REVIEW:
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        return event;
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(MINIMUM_HOURS_BEFORE_EVENT_ADMIN))) {
            log.error("Дата события {} раньше чем через час от текущего времени", dateTime);
            throw new ConflictException("Дата начала события меньше чем час " + dateTime);
        }
    }

    private User getUser(Long userId) {
        log.debug("Поиск пользователя с ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userId);
                    return new NotFoundException("Пользователь не найден");
                });
    }

    private Category getCategory(Long categoryId) {
        log.debug("Поиск категории с ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Категория с ID {} не найдена", categoryId);
                    return new NotFoundException("Категория не найдена");
                });
    }

    private void updateEventFields(UpdateEventDto eventDto, Event foundEvent) {
        log.debug("Обновление полей события с ID {}: {}", foundEvent.getId(), eventDto);
        if (eventDto.getCategory() != null) {
            Category category = getCategory(eventDto.getCategory());
            foundEvent.setCategory(category);
        }
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            foundEvent.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            foundEvent.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(MINIMUM_HOURS_BEFORE_EVENT_USER))) {
                log.error("Дата события {} раньше чем через 2 часа от текущего времени", eventDto.getEventDate());
                throw new ConflictException("Дата начала события не может быть раньше чем через 2 часа");
            }
            foundEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) {
            foundEvent.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            if (eventDto.getParticipantLimit() < DEFAULT_PARTICIPANT_LIMIT) {
                log.error("Лимит участников {} меньше нуля", eventDto.getParticipantLimit());
                throw new ValidationException("Participant limit cannot be negative");
            }
            foundEvent.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            foundEvent.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            foundEvent.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            if (eventDto.getLocation().getLat() != null) {
                foundEvent.setLat(eventDto.getLocation().getLat());
            }
            if (eventDto.getLocation().getLon() != null) {
                foundEvent.setLon(eventDto.getLocation().getLon());
            }
        }
        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case CANCEL_REVIEW -> foundEvent.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> foundEvent.setState(EventState.PUBLISHED);
                case SEND_TO_REVIEW -> foundEvent.setState(EventState.PENDING);
            }
        }
    }

    private List<String> getListOfUri(List<Event> events, String uri) {
        log.debug("Генерация списка URI для событий");
        return events.stream().map(Event::getId).map(id -> getUriForEvent(uri, id))
                .collect(Collectors.toList());
    }

    private String getUriForEvent(String uri, Long eventId) {
        return uri + "/" + eventId;
    }

    private Request changeStatus(Request request, RequestStatus status) {
        log.debug("Изменение статуса запроса с ID {} на {}", request.getId(), status);
        request.setStatus(status);
        return request;
    }

    private void checkRequestsStatus(List<Request> requests) {
        Optional<Request> confirmedReq = requests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                .findFirst();
        if (confirmedReq.isPresent()) {
            log.error("Попытка отклонить уже подтвержденный запрос с ID {}", confirmedReq.get().getId());
            throw new ConflictException("Нельзя отменить уже принятую заявку.");
        }
    }
}