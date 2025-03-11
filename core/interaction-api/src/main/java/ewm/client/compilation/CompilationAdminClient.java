package ewm.client.compilation;

import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CompilationDtoResponse;
import ewm.dto.compilation.CompilationDtoUpdate;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "compilation-admin-client")
public interface CompilationAdminClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    CompilationDtoResponse create(@RequestBody @Valid CompilationDto compilationDto);

    @PatchMapping("/{compId}")
    CompilationDtoResponse update(@PathVariable Long compId,
                                  @RequestBody @Valid CompilationDtoUpdate compilationDto);

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    void delete(@PathVariable Long compId);

}
