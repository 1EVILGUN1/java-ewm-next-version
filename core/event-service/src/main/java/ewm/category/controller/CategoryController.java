package ewm.category.controller;

import ewm.category.service.CategoryService;
import ewm.dto.category.CategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Received request to get categories with from: {} and size: {}", from, size);
        List<CategoryDto> result = service.getAll(from, size);
        log.info("Successfully retrieved {} categories", result.size());
        return result;
    }

    @GetMapping("/{categoryId}")
    public CategoryDto getCategory(@PathVariable Long categoryId) {
        log.info("Received request to get category with id: {}", categoryId);
        CategoryDto result = service.getById(categoryId);
        log.info("Successfully retrieved category with id: {}", categoryId);
        return result;
    }
}