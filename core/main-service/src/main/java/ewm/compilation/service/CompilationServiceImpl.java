package ewm.compilation.service;

import ewm.compilation.mapper.CompilationMapper;
import ewm.compilation.repositry.CompilationRepository;
import ewm.dto.compilation.CompilationDto;
import ewm.dto.compilation.CompilationDtoResponse;
import ewm.dto.compilation.CompilationDtoUpdate;
import ewm.error.exception.NotFoundException;
import ewm.event.repository.EventRepository;
import ewm.model.compilation.Compilation;
import ewm.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private static final String COMPILATION_NOT_FOUND = "Подборки с id = %d не существует";

    private final CompilationRepository repository;
    private final EventRepository eventRepository;
    private final CompilationMapper mapper = CompilationMapper.INSTANCE;

    @Transactional
    @Override
    public CompilationDtoResponse create(CompilationDto compilationDto) {
        log.info("Создание новой подборки: {}", compilationDto);
        List<Event> events = compilationDto.getEvents() == null
                ? new ArrayList<>()
                : eventRepository.findAllById(compilationDto.getEvents());

        Compilation compilation = Compilation.builder()
                .events(events)
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .build();

        CompilationDtoResponse result = mapper.compilationToCompilationDtoResponse(repository.save(compilation));
        log.info("Подборка успешно создана: {}", result);
        return result;
    }

    @Transactional
    @Override
    public CompilationDtoResponse update(Long compId, CompilationDtoUpdate compilationDto) {
        log.info("Обновление подборки с ID {}: {}", compId, compilationDto);
        Compilation compilation = getCompilationOrThrow(compId);

        updateEventsIfPresent(compilation, compilationDto.getEvents());
        updatePinnedIfPresent(compilation, compilationDto.getPinned());
        updateTitleIfPresent(compilation, compilationDto.getTitle());

        CompilationDtoResponse result = mapper.compilationToCompilationDtoResponse(repository.save(compilation));
        log.info("Подборка с ID {} успешно обновлена: {}", compId, result);
        return result;
    }

    @Transactional
    @Override
    public void delete(Long compId) {
        log.info("Удаление подборки с ID: {}", compId);
        getCompilationOrThrow(compId);
        repository.deleteById(compId);
        log.info("Подборка с ID {} успешно удалена", compId);
    }

    @Override
    public List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение списка подборок с параметрами: pinned = {}, from = {}, size = {}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = (pinned != null)
                ? repository.findByPinned(pinned, pageable)
                : repository.findAll(pageable).getContent();

        List<CompilationDtoResponse> result = mapper.mapListCompilations(compilations);
        log.info("Успешно получен список подборок, количество: {}", result.size());
        return result;
    }

    @Override
    public CompilationDtoResponse getCompilation(Long compId) {
        log.info("Получение подборки с ID: {}", compId);
        CompilationDtoResponse result = mapper.compilationToCompilationDtoResponse(getCompilationOrThrow(compId));
        log.info("Подборка с ID {} успешно получена: {}", compId, result);
        return result;
    }

    private Compilation getCompilationOrThrow(Long compId) {
        log.debug("Поиск подборки с ID: {}", compId);
        return repository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Подборка с ID {} не найдена", compId);
                    return new NotFoundException(String.format(COMPILATION_NOT_FOUND, compId));
                });
    }

    private void updateEventsIfPresent(Compilation compilation, List<Long> eventIds) {
        if (eventIds != null && !eventIds.isEmpty()) {
            log.debug("Обновление событий для подборки, новые ID событий: {}", eventIds);
            compilation.setEvents(eventRepository.findAllById(eventIds));
        }
    }

    private void updatePinnedIfPresent(Compilation compilation, Boolean pinned) {
        if (pinned != null) {
            log.debug("Обновление статуса pinned для подборки: {}", pinned);
            compilation.setPinned(pinned);
        }
    }

    private void updateTitleIfPresent(Compilation compilation, String title) {
        if (title != null) {
            log.debug("Обновление заголовка подборки: {}", title);
            compilation.setTitle(title);
        }
    }
}