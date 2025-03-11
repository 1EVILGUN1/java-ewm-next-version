package ewm.event.mapper;

import ewm.category.mapper.CategoryMapper;
import ewm.dto.event.CreateEventDto;
import ewm.dto.event.EventDto;
import ewm.dto.event.LocationDto;
import ewm.dto.event.UpdateEventDto;
import ewm.model.event.Event;
import ewm.user.mapper.UserMapper;

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

    public static EventDto mapEventToEventDto(Event event) {
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
                .category(CategoryMapper.INSTANCE.categoryToCategoryDto(event.getCategory()))
                .initiator(UserMapper.mapToUserDto(event.getInitiator()))
                .requestModeration(event.getRequestModeration())
                .views((event.getViews() == null) ? 0L : event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
        return dto;
    }

    public static List<EventDto> mapToEventDto(Iterable<Event> events) {
        List<EventDto> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(mapEventToEventDto(event));
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
}
