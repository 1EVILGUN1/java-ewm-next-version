package ewm.category.service;

import ewm.dto.category.CategoryDto;
import ewm.dto.category.CreateCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto add(CreateCategoryDto createCategoryDto);

    CategoryDto update(Long id, CreateCategoryDto createCategoryDto);

    void delete(Long id);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long id);
}
