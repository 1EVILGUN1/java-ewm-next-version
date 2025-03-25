package ewm.compilation.controller.admin;

import ewm.compilation.service.CompilationService;
import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CompilationDtoResponse;
import ewm.dto.compilation.CompilationDtoUpdate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("admin/compilations")
public class CompilationAdminController {
    private final CompilationService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompilationDtoResponse createCompilation(@RequestBody @Valid CompilationDto compilationDto) {
        log.info("Добавить подборку Compilation --> {}", compilationDto);
        return service.createCompilation(compilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Удалить подборку по compId --> {}", compId);
        service.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDtoResponse updateCompilation(@PathVariable Long compId,
                                                    @RequestBody @Valid CompilationDtoUpdate compilationDto) {
        log.info("Обновить подборку по compId --> {}, подборка --> {}", compId, compilationDto);
        return service.updateCompilation(compId, compilationDto);
    }
}
