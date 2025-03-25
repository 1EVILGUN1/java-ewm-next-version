package ewm.compilation.service;

import ewm.compilation.model.Compilation;
import ewm.compilation.repository.CompilationRepository;
import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CompilationDtoResponse;
import ewm.dto.compilation.CompilationDtoUpdate;
import ewm.error.exception.NotFoundException;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.mapper.CompilationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    // Зависимости
    private final CompilationRepository repository;
    private final EventRepository eventRepository;

    // Методы для создания и обновления подборок
    @Transactional
    @Override
    public CompilationDtoResponse createCompilation(CompilationDto compilationDto) {
        log.info("Создание новой подборки с данными: {}", compilationDto);
        List<Event> events = getEventsForCompilation(compilationDto.getEvents());
        log.debug("Найдено {} событий для подборки", events.size());
        Compilation compilation = Compilation.builder()
                .events(events)
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .build();
        Compilation savedCompilation = repository.save(compilation);
        log.info("Подборка успешно создана с id: {}", savedCompilation.getId());
        return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(savedCompilation);
    }

    @Transactional
    @Override
    public CompilationDtoResponse updateCompilation(Long compId, CompilationDtoUpdate compilationDto) {
        log.info("Обновление подборки с id: {} данными: {}", compId, compilationDto);
        Compilation compilation = getCompFromRepo(compId);
        updateCompilationFields(compilation, compilationDto);
        Compilation updatedCompilation = repository.save(compilation);
        log.info("Подборка с id: {} успешно обновлена", compId);
        return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(updatedCompilation);
    }

    // Методы для удаления и получения подборок
    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);
        Compilation compilation = getCompFromRepo(compId);
        repository.deleteById(compId);
        log.info("Подборка с id: {} успешно удалена", compId);
    }

    @Override
    public List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение списка подборок с параметрами: pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageRequest = PageRequest.of(from / size, size);
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = repository.findByPinned(pinned, pageRequest);
        } else {
            compilations = repository.findAll(pageRequest).getContent();
        }
        log.info("Найдено {} подборок", compilations.size());
        return CompilationMapper.INSTANCE.mapListCompilations(compilations);
    }

    @Override
    public CompilationDtoResponse getCompilation(Long compId) {
        log.info("Получение подборки с id: {}", compId);
        Compilation compilation = getCompFromRepo(compId);
        log.info("Подборка с id: {} успешно получена", compId);
        return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(compilation);
    }

    // Вспомогательные методы
    private Compilation getCompFromRepo(Long compId) {
        log.debug("Поиск подборки с id: {}", compId);
        return repository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Подборка с id: {} не найдена", compId);
                    return new NotFoundException("Подборка с id = " + compId + " не существует");
                });
    }

    private List<Event> getEventsForCompilation(List<Long> eventIds) {
        log.debug("Получение событий для подборки по id: {}", eventIds);
        List<Event> events = eventIds == null || eventIds.isEmpty()
                ? new ArrayList<>()
                : eventRepository.findAllById(eventIds);
        log.debug("Найдено {} событий", events.size());
        return events;
    }

    private void updateCompilationFields(Compilation compilation, CompilationDtoUpdate compilationDto) {
        log.debug("Обновление полей подборки с id: {}", compilation.getId());
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            compilation.setEvents(events);
            log.debug("Обновлены события подборки, количество: {}", events.size());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
            log.debug("Обновлено поле pinned: {}", compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
            log.debug("Обновлено поле title: {}", compilationDto.getTitle());
        }
    }
}