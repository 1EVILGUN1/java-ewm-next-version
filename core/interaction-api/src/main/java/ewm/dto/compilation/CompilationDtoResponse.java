package ewm.dto.compilation;

import ewm.model.event.Event;
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
    private List<Event> events;
    private Boolean pinned;
    private String title;
}
