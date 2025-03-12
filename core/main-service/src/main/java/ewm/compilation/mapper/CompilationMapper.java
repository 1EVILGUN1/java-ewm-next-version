package ewm.compilation.mapper;

import ewm.dto.compilation.CompilationDtoResponse;
import ewm.model.compilation.Compilation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CompilationMapper {
    CompilationMapper INSTANCE = Mappers.getMapper(CompilationMapper.class);

    CompilationDtoResponse compilationToCompilationDtoResponse(Compilation compilation);

    List<CompilationDtoResponse> mapListCompilations(List<Compilation> compilations);
}
