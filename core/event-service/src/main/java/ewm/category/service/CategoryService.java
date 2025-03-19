package ewm.category.service;

import ewm.dto.category.CategoryDto;
import ewm.dto.category.CreateCategoryDto;

import java.util.List;

// Управление категориями
public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long id);

    CategoryDto add(CreateCategoryDto createCategoryDto);

    CategoryDto update(Long id, CreateCategoryDto createCategoryDto);

    void delete(Long id);
}
