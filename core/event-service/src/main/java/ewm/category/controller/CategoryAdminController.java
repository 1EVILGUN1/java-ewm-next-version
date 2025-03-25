package ewm.category.controller;

import ewm.category.service.CategoryService;
import ewm.dto.category.CategoryDto;
import ewm.dto.category.CreateCategoryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("admin/categories")
@Slf4j
public class CategoryAdminController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CreateCategoryDto createCategoryDto) {
        log.info("Received request to add new category: {}", createCategoryDto);
        CategoryDto result = service.add(createCategoryDto);
        log.info("Category successfully added with id: {}", result.getId());
        return result;
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        log.info("Received request to delete category with id: {}", categoryId);
        service.delete(categoryId);
        log.info("Category with id: {} successfully deleted", categoryId);
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto updateCategory(@PathVariable Long categoryId, @RequestBody @Valid CreateCategoryDto createCategoryDto) {
        log.info("Received request to update category with id: {} and data: {}", categoryId, createCategoryDto);
        CategoryDto result = service.update(categoryId, createCategoryDto);
        log.info("Category with id: {} successfully updated", categoryId);
        return result;
    }
}