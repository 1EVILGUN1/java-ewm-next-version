package ewm.mapper;

import ewm.dto.event.CreateEventDto;
import ewm.dto.event.EventDto;
import ewm.dto.event.UpdateEventDto;
import ewm.dto.event.UpdatedEventDto;
import ewm.dto.user.UserDto;
import ewm.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    // CreateEventDto → Event
    @Mapping(source = "eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(source = "location.lat", target = "lat")
    @Mapping(source = "location.lon", target = "lon")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    Event mapCreateDtoToEvent(CreateEventDto dto);

    // Event → EventDto (без UserDto)
    @Mapping(source = "category.id", target = "category.id")
    @Mapping(source = "lat", target = "location.lat")
    @Mapping(source = "lon", target = "location.lon")
    @Mapping(source = "state", target = "state", qualifiedByName = "enumToString")
    @Mapping(source = "initiatorId", target = "initiator.id")
    @Mapping(source = "views", target = "views", defaultValue = "0L")
    @Mapping(source = "confirmedRequests", target = "confirmedRequests", defaultValue = "0")
    EventDto mapEventToEventDto(Event event);

    // Event → EventDto (с UserDto для совместимости с сервисом)
    @Mapping(source = "event.id", target = "id") // Явно указываем источник id
    @Mapping(source = "event.category.id", target = "category.id")
    @Mapping(source = "event.lat", target = "location.lat")
    @Mapping(source = "event.lon", target = "location.lon")
    @Mapping(source = "event.state", target = "state", qualifiedByName = "enumToString")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(source = "event.views", target = "views", defaultValue = "0L")
    @Mapping(source = "event.confirmedRequests", target = "confirmedRequests", defaultValue = "0")
    EventDto mapEventToEventDto(Event event, UserDto initiator);

    List<EventDto> mapToEventDto(List<Event> events);

    // UpdateEventDto → Event
    @Mapping(source = "eventDate", target = "eventDate")
    @Mapping(source = "location.lat", target = "lat", nullValueCheckStrategy = org.mapstruct.NullValueCheckStrategy.ALWAYS)
    @Mapping(source = "location.lon", target = "lon", nullValueCheckStrategy = org.mapstruct.NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    Event mapUpdateDtoToEvent(UpdateEventDto dto);

    // Event → UpdatedEventDto (без UserDto)
    @Mapping(source = "category.id", target = "category.id")
    @Mapping(source = "lat", target = "location.lat")
    @Mapping(source = "lon", target = "location.lon")
    @Mapping(source = "state", target = "state", qualifiedByName = "enumToString")
    @Mapping(source = "initiatorId", target = "initiator.id")
    @Mapping(source = "views", target = "views", defaultValue = "0L")
    @Mapping(source = "confirmedRequests", target = "confirmedRequests", defaultValue = "0")
    UpdatedEventDto mapEventToUpdatedEventDto(Event event);

    // Event → UpdatedEventDto (с UserDto для совместимости с сервисом)
    @Mapping(source = "event.id", target = "id") // Явно указываем источник id
    @Mapping(source = "event.category.id", target = "category.id")
    @Mapping(source = "event.lat", target = "location.lat")
    @Mapping(source = "event.lon", target = "location.lon")
    @Mapping(source = "event.state", target = "state", qualifiedByName = "enumToString")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(source = "event.views", target = "views", defaultValue = "0L")
    @Mapping(source = "event.confirmedRequests", target = "confirmedRequests", defaultValue = "0")
    UpdatedEventDto mapEventToUpdatedEventDto(Event event, UserDto initiator);

    List<UpdatedEventDto> mapToUpdatedEventDto(List<Event> events);

    // EventDto → Event
    @Mapping(source = "category.id", target = "category.id")
    @Mapping(source = "initiator.id", target = "initiatorId")
    @Mapping(source = "location.lat", target = "lat")
    @Mapping(source = "location.lon", target = "lon")
    @Mapping(source = "state", target = "state", qualifiedByName = "stringToEnum")
    Event mapToEvent(EventDto dto);

    @org.mapstruct.Named("enumToString")
    default String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.toString() : null;
    }

    @org.mapstruct.Named("stringToEnum")
    default ewm.enums.EventState stringToEnum(String state) {
        return state != null ? ewm.enums.EventState.valueOf(state) : null;
    }
}