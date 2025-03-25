package ewm.compilation.controller.pub;

import ewm.compilation.service.CompilationService;
import ewm.dto.compilation.CompilationDtoResponse;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("compilations")
public class CompilationPubController {
    private final CompilationService service;

    @GetMapping
    public List<CompilationDtoResponse> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                        @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получить список подборок по pinned --> {}, from --> {}, size --> {}", pinned, from, size);
        List<CompilationDtoResponse> result = service.getCompilations(pinned, from, size);
        log.info("Успешно получено {} подборок", result.size());
        return result;
    }

    @GetMapping("/{compId}")
    public CompilationDtoResponse getCompilations(@PathVariable Long compId) {
        log.info("Получить подборку по compId --> {}", compId);
        CompilationDtoResponse result = service.getCompilation(compId);
        log.info("Успешно получена подборка с id: {}", compId);
        return result;
    }
}