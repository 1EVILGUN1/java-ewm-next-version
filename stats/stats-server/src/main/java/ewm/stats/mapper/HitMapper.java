package ewm.stats.mapper;

import ewm.dto.EndpointHitResponseDto;
import ewm.stats.model.Hit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HitMapper {
    EndpointHitResponseDto hitToEndpointHitResponseDto(Hit hit);
}
