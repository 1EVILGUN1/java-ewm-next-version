package ewm.dto.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LocationDto {
    private Double lat;
    private Double lon;
}
