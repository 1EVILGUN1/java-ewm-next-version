package ewm.compilation.service;

import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CompilationDtoResponse;
import ewm.dto.compilation.CompilationDtoUpdate;

import java.util.List;

public interface CompilationService {
    CompilationDtoResponse create(CompilationDto compilationDto);

    CompilationDtoResponse update(Long compId, CompilationDtoUpdate compilationDto);

    void delete(Long compId);

    List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDtoResponse getCompilation(Long compId);
}
