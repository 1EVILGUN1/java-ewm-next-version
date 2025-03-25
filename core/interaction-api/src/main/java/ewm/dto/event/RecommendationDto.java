package ewm.dto.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RecommendationDto {
    private Long eventId;
    private Double score;
}
