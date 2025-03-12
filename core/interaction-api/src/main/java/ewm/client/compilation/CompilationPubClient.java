package ewm.client.compilation;

import ewm.dto.compilation.CompilationDtoResponse;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "compilation-pub-client")
public interface CompilationPubClient {

    @GetMapping
    List<CompilationDtoResponse> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size);

    @GetMapping("/{compId}")
    CompilationDtoResponse getCompilations(@PathVariable Long compId);
}
