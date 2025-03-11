package ewm.category.service;

import ewm.category.mapper.CategoryMapper;
import ewm.category.repository.CategoryRepository;
import ewm.dto.category.CategoryDto;
import ewm.dto.category.CreateCategoryDto;
import ewm.error.exception.ConflictException;
import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.event.repository.EventRepository;
import ewm.model.category.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private static final String CATEGORY_NOT_FOUND = "Category not found";
    private static final String CATEGORY_NAME_EXISTS = "Category with this name already exist";
    private static final String EVENTS_ATTACHED = "Есть привязанные события.";

    private final CategoryRepository repository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper = CategoryMapper.INSTANCE;

    @Override
    public CategoryDto add(CreateCategoryDto createCategoryDto) {
        log.info("Создание новой категории с именем: {}", createCategoryDto.getName());
        Category category = Category.builder()
                .name(createCategoryDto.getName())
                .build();
        CategoryDto savedCategory = saveAndMap(category);
        log.info("Категория успешно создана: {}", savedCategory);
        return savedCategory;
    }

    @Override
    public CategoryDto update(Long id, CreateCategoryDto createCategoryDto) {
        log.info("Обновление категории с ID={}", id);
        Category category = getCategoryOrThrow(id);
        log.info("Текущее имя: {}, новое имя: {}", category.getName(), createCategoryDto.getName());
        category.setName(createCategoryDto.getName());
        CategoryDto updatedCategory = saveAndMap(category);
        log.info("Категория успешно обновлена: {}", updatedCategory);
        return updatedCategory;
    }

    @Override
    public void delete(Long id) {
        log.info("Удаление категории с ID={}", id);
        Category category = getCategoryOrThrow(id);
        log.info("Проверка наличия привязанных событий для категории: {}", category.getName());
        checkEventsAttached(id);
        repository.deleteById(id);
        log.info("Категория с ID={} успешно удалена", id);
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        log.info("Получение списка категорий с пагинацией: from={}, size={}", from, size);
        Pageable pageable = PageRequest.of(from, size);
        List<CategoryDto> categories = repository.findAll(pageable)
                .map(mapper::categoryToCategoryDto)
                .toList();
        log.info("Найдено категорий: {}", categories.size());
        return categories;
    }

    @Override
    public CategoryDto getById(Long id) {
        log.info("Получение категории по ID={}", id);
        CategoryDto categoryDto = mapper.categoryToCategoryDto(getCategoryOrThrow(id));
        log.info("Категория найдена: {}", categoryDto);
        return categoryDto;
    }

    private Category getCategoryOrThrow(Long id) {
        log.info("Поиск категории по ID={}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Категория с ID={} не найдена", id);
                    return new NotFoundException(CATEGORY_NOT_FOUND);
                });
    }

    private void checkEventsAttached(Long id) {
        log.info("Проверка привязанных событий для категории с ID={}", id);
        if (!eventRepository.findByCategoryId(id).isEmpty()) {
            log.warn("Найдены привязанные события для категории с ID={}", id);
            throw new ConflictException(EVENTS_ATTACHED);
        }
        log.info("Привязанные события отсутствуют");
    }

    private CategoryDto saveAndMap(Category category) {
        try {
            log.info("Сохранение категории: {}", category.getName());
            CategoryDto savedCategory = mapper.categoryToCategoryDto(repository.save(category));
            log.info("Категория успешно сохранена в БД");
            return savedCategory;
        } catch (DataAccessException e) {
            log.error("Ошибка сохранения: категория с именем '{}' уже существует", category.getName());
            throw new ExistException(CATEGORY_NAME_EXISTS);
        }
    }
}