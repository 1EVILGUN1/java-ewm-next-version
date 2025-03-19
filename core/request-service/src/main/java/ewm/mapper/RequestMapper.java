package ewm.mapper;

import ewm.dto.request.RequestDto;
import ewm.model.Request;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(source = "eventId", target = "event") // eventId из Request мапится на event в RequestDto
    @Mapping(source = "requesterId", target = "requester") // requesterId из Request мапится на requester в RequestDto
    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
        // LocalDateTime в String
    RequestDto mapToRequestDto(Request request);

    List<RequestDto> mapListRequests(List<Request> requests);

    @Mapping(source = "event", target = "eventId") // event из RequestDto мапится на eventId в Request
    @Mapping(source = "requester", target = "requesterId") // requester из RequestDto мапится на requesterId в Request
    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
        // String в LocalDateTime
    Request mapDtoToRequest(RequestDto requestDto);

    List<Request> mapDtoToRequestList(List<RequestDto> requestDtoList);
}