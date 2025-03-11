package ewm.event.service;

import ewm.dto.event.*;
import ewm.dto.request.RequestDto;
import ewm.model.event.Event;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface EventService {

    EventDto create(Long userId, CreateEventDto eventDto);

    EventDto update(Long userId, UpdateEventDto eventDto, Long eventId);

    List<EventDto> getEvents(Long userId, Integer from, Integer size);

    EventDto getEventById(Long userId, Long eventId, String ip, String uri);

    List<EventDto> adminGetEvents(AdminGetEventRequestDto requestParams);

    EventDto adminChangeEvent(Long eventId, UpdateEventDto eventDto);

    List<EventDto> publicGetEvents(PublicGetEventRequestDto requestParams, HttpServletRequest request);

    EventDto publicGetEvent(Long eventId, HttpServletRequest request);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStatusEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    Event getEventByInitiator(Long userId);
}
