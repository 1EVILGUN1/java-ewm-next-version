package ewm.event.service;

import ewm.dto.event.*;
import ewm.dto.request.RequestDto;

import java.util.List;

public interface EventService {
    List<EventDto> getEvents(Long userId, Integer from, Integer size);

    EventDto getEventById(Long userId, Long eventId, String ip, String uri);

    UpdatedEventDto createEvent(Long userId, CreateEventDto eventDto);

    List<UpdatedEventDto> adminGetEvents(AdminGetEventRequestDto requestParams);

    UpdatedEventDto adminChangeEvent(Long eventId, UpdateEventDto eventDto);

    UpdatedEventDto updateEvent(Long userId, UpdateEventDto eventDto, Long eventId);

    List<UpdatedEventDto> publicGetEvents(PublicGetEventRequestDto requestParams, long userId);

    UpdatedEventDto publicGetEvent(Long eventId, long userId);

    EventDto publicGetEvent(Long eventId);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStatusEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    EventDto updateConfirmRequests(EventDto eventDto);

    List<RecommendationDto> getRecommendations(Long limit, long userId);

    void saveLike(Long eventId, long userId);

    EventDto getEventByInitiator(Long userId);
}
