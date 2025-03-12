package ewm.client.category;

import ewm.dto.category.CategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "category-client")
public interface CategoryClient {
    @GetMapping
    List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    );

    @GetMapping("/{categoryId}")
    CategoryDto getCategory(@PathVariable Long categoryId);
}
