package ewm.mapper;


import ewm.category.model.Category;
import ewm.dto.category.CategoryDto;
import ewm.dto.event.*;
import ewm.dto.user.UserDto;
import ewm.enums.EventState;
import ewm.event.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class EventMapper {

    public static Event mapCreateDtoToEvent(CreateEventDto dto) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(dto.getDescription());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dto.getEventDate(), formatter);
        event.setEventDate(dateTime);
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setTitle(dto.getTitle());
        event.setLat(dto.getLocation().getLat());
        event.setLon(dto.getLocation().getLon());
        return event;
    }

    public static EventDto mapEventToEventDto(Event event, UserDto initiator) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(event.getCategory().getId());
        EventDto dto = EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .state(event.getState().toString())
                .participantLimit(event.getParticipantLimit())
                .location(LocationDto.builder().lat(event.getLat()).lon(event.getLon()).build())
                .category(categoryDto)
                .initiator(initiator)
                .requestModeration(event.getRequestModeration())
                .rating((event.getRating() == null) ? 0L : event.getRating())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
        return dto;
    }

    public static List<EventDto> mapToEventDto(Iterable<Event> events) {
        List<EventDto> dtos = new ArrayList<>();
        for (Event event : events) {
            UserDto userDto = new UserDto();
            userDto.setId(event.getInitiatorId());
            dtos.add(mapEventToEventDto(event, userDto));
        }
        return dtos;
    }

    public static Event mapUpdateDtoToEvent(UpdateEventDto dto) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(dto.getDescription());
        LocalDateTime dateTime = dto.getEventDate();
        event.setEventDate(dateTime);
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setTitle(dto.getTitle());
        if (dto.getLocation() != null) {
            event.setLat(dto.getLocation().getLat());
            event.setLon(dto.getLocation().getLon());
        }
        return event;
    }

    public static Event mapToEvent(EventDto dto) {
        Event event = new Event();
        event.setId(dto.getId());
        event.setInitiatorId(dto.getInitiator().getId());
        event.setEventDate(dto.getEventDate());
        event.setPaid(dto.getPaid());
        event.setAnnotation(dto.getAnnotation());
        event.setCreatedOn(dto.getCreatedOn());
        event.setDescription(dto.getDescription());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setPublishedOn(dto.getPublishedOn());
        event.setRequestModeration(dto.getRequestModeration());
        event.setState(EventState.valueOf(dto.getState()));
        event.setTitle(dto.getTitle());
        Category c = new Category();
        c.setId(dto.getCategory().getId());
        event.setCategory(c);
        event.setInitiatorId(dto.getInitiator().getId());
        event.setLat(dto.getLocation().getLat());
        event.setLon(dto.getLocation().getLon());
        event.setRating(dto.getRating());
        event.setConfirmedRequests(dto.getConfirmedRequests());

        return event;
    }

    public static UpdatedEventDto mapEventToUpdatedEventDto(Event event, UserDto initiator) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(event.getCategory().getId());
        UpdatedEventDto dto = UpdatedEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .state(event.getState().toString())
                .participantLimit(event.getParticipantLimit())
                .location(LocationDto.builder().lat(event.getLat()).lon(event.getLon()).build())
                .category(categoryDto)
                .initiator(initiator)
                .requestModeration(event.getRequestModeration())
                .rating((event.getRating() == null) ? 0L : event.getRating())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
        return dto;
    }

    public static List<UpdatedEventDto> mapToUpdatedEventDto(Iterable<Event> events) {
        List<UpdatedEventDto> dtos = new ArrayList<>();
        for (Event event : events) {
            UserDto userDto = new UserDto();
            userDto.setId(event.getInitiatorId());
            dtos.add(mapEventToUpdatedEventDto(event, userDto));
        }
        return dtos;
    }
}
