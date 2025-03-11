package ewm.category.controller;

import ewm.category.service.CategoryService;
import ewm.client.category.CategoryClient;
import ewm.dto.category.CategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController implements CategoryClient {
    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Получен публичный запрос на получение списка категорий");
        log.info("Параметры пагинации: from={}, size={}", from, size);

        List<CategoryDto> categories = service.getAll(from, size);
        log.info("Найдено категорий: {}", categories.size());

        return categories;
    }

    @GetMapping("/{categoryId}")
    public CategoryDto getCategory(@PathVariable Long categoryId) {
        log.info("Получен публичный запрос на получение категории с ID={}", categoryId);

        CategoryDto category = service.getById(categoryId);
        log.info("Категория успешно найдена: {}", category);

        return category;
    }
}