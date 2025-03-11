package ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CompilationDto {

    private List<Long> events;
    private Boolean pinned = false;

    @NotBlank
    @Size(min = 1, message = "{validation.title.size.too_short}")
    @Size(max = 50, message = "{validation.title.size.too_long}")
    private String title;
}
