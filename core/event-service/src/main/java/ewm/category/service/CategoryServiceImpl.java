package ewm.category.service;

import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.dto.category.CategoryDto;
import ewm.dto.category.CreateCategoryDto;
import ewm.error.exception.ConflictException;
import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private static final String CATEGORY_NOT_FOUND = "Category not found";
    private static final String CATEGORY_NAME_EXIST = "Category with this name already exist";

    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        log.info("Fetching categories with from: {} and size: {}", from, size);
        Pageable pageable = PageRequest.of(from, size);
        List<CategoryDto> result = repository.findAll(pageable).stream()
                .map(CategoryMapper.INSTANCE::categoryToCategoryDto)
                .toList();
        log.info("Retrieved {} categories", result.size());
        return result;
    }

    @Override
    public CategoryDto getById(Long id) {
        log.info("Fetching category by id: {}", id);
        Optional<Category> category = repository.findById(id);
        if (category.isEmpty()) {
            log.warn("Category with id: {} not found", id);
            throw new NotFoundException(CATEGORY_NOT_FOUND);
        }
        CategoryDto result = CategoryMapper.INSTANCE.categoryToCategoryDto(category.get());
        log.info("Successfully retrieved category with id: {}", id);
        return result;
    }

    @Override
    public CategoryDto add(CreateCategoryDto createCategoryDto) {
        log.info("Adding new category with data: {}", createCategoryDto);
        try {
            Category category = repository.save(Category.builder().name(createCategoryDto.getName()).build());
            CategoryDto result = CategoryMapper.INSTANCE.categoryToCategoryDto(category);
            log.info("Category successfully added with id: {}", result.getId());
            return result;
        } catch (DataAccessException e) {
            log.error("Failed to add category due to existing name: {}", createCategoryDto.getName(), e);
            throw new ExistException(CATEGORY_NAME_EXIST);
        }
    }

    @Override
    public CategoryDto update(Long id, CreateCategoryDto createCategoryDto) {
        log.info("Updating category with id: {} and data: {}", id, createCategoryDto);
        Optional<Category> category = repository.findById(id);
        if (category.isEmpty()) {
            log.warn("Category with id: {} not found for update", id);
            throw new NotFoundException(CATEGORY_NOT_FOUND);
        }
        Category categoryToUpdate = category.get();
        categoryToUpdate.setName(createCategoryDto.getName());
        try {
            Category updated = repository.save(categoryToUpdate);
            CategoryDto result = CategoryMapper.INSTANCE.categoryToCategoryDto(updated);
            log.info("Category with id: {} successfully updated", id);
            return result;
        } catch (DataAccessException e) {
            log.error("Failed to update category with id: {} due to existing name: {}", id, createCategoryDto.getName(), e);
            throw new ExistException(CATEGORY_NAME_EXIST);
        }
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting category with id: {}", id);
        Optional<Category> category = repository.findById(id);
        if (category.isEmpty()) {
            log.warn("Category with id: {} not found for deletion", id);
            throw new NotFoundException(CATEGORY_NOT_FOUND);
        }
        List<Event> events = eventRepository.findByCategoryId(id);
        if (!events.isEmpty()) {
            log.warn("Cannot delete category with id: {} due to {} associated events", id, events.size());
            throw new ConflictException("Есть привязанные события.");
        }
        repository.deleteById(id);
        log.info("Category with id: {} successfully deleted", id);
    }
}