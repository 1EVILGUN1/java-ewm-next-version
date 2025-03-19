package ewm.dto.compilation;

import ewm.dto.event.EventDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CompilationDtoResponse {
    private Long id;
    private List<EventDto> events;
    private Boolean pinned;
    private String title;
}
