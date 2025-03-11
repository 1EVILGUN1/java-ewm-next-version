package ewm.compilation.controller;

import ewm.client.compilation.CompilationAdminClient;
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
public class CompilationAdminController implements CompilationAdminClient {
    private final CompilationService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompilationDtoResponse create(@RequestBody @Valid CompilationDto compilationDto) {
        log.info("Получен запрос на создание подборки: {}", compilationDto);
        CompilationDtoResponse result = service.create(compilationDto);
        log.info("Подборка успешно создана: {}", result);
        return result;
    }

    @PatchMapping("/{compId}")
    public CompilationDtoResponse update(@PathVariable Long compId,
                                         @RequestBody @Valid CompilationDtoUpdate compilationDto) {
        log.info("Получен запрос на обновление подборки с ID {}: {}", compId, compilationDto);
        CompilationDtoResponse result = service.update(compId, compilationDto);
        log.info("Подборка с ID {} успешно обновлена: {}", compId, result);
        return result;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void delete(@PathVariable Long compId) {
        log.info("Получен запрос на удаление подборки с ID: {}", compId);
        service.delete(compId);
        log.info("Подборка с ID {} успешно удалена", compId);
    }
}