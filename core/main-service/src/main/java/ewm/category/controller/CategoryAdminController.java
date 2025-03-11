package ewm.category.controller;

import ewm.category.service.CategoryService;
import ewm.client.category.CategoryAdminClient;
import ewm.dto.category.CategoryDto;
import ewm.dto.category.CreateCategoryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("admin/categories")
public class CategoryAdminController implements CategoryAdminClient {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@RequestBody @Valid CreateCategoryDto createCategoryDto) {
        log.info("Получен запрос на создание новой категории администратором");
        log.info("Данные новой категории: {}", createCategoryDto);

        CategoryDto createdCategory = service.add(createCategoryDto);
        log.info("Категория успешно создана: {}", createdCategory);

        return createdCategory;
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto update(@PathVariable Long categoryId,
                              @RequestBody @Valid CreateCategoryDto createCategoryDto) {
        log.info("Получен запрос на обновление категории администратором");
        log.info("ID категории для обновления: {}, новые данные: {}", categoryId, createCategoryDto);

        CategoryDto updatedCategory = service.update(categoryId, createCategoryDto);
        log.info("Категория успешно обновлена: {}", updatedCategory);

        return updatedCategory;
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long categoryId) {
        log.info("Получен запрос на удаление категории администратором");
        log.info("ID категории для удаления: {}", categoryId);

        service.delete(categoryId);
        log.info("Категория с ID={} успешно удалена", categoryId);
    }
}