package ewm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsRequestDTO {

    @NotNull
    private String start;

    @NotNull
    private String end;

    @Builder.Default
    private List<String> uris = new ArrayList<>();

    @Builder.Default
    private Boolean unique = false;
}