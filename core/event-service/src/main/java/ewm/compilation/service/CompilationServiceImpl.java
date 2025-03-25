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
		List<Event> events = getEventsForCompilation(compilationDto.getEvents());
		Compilation compilation = Compilation.builder()
				.events(events)
				.title(compilationDto.getTitle())
				.pinned(compilationDto.getPinned())
				.build();
		Compilation savedCompilation = repository.save(compilation);
		return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(savedCompilation);
	}

	@Transactional
	@Override
	public CompilationDtoResponse updateCompilation(Long compId, CompilationDtoUpdate compilationDto) {
		Compilation compilation = getCompFromRepo(compId);
		updateCompilationFields(compilation, compilationDto);
		Compilation updatedCompilation = repository.save(compilation);
		return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(updatedCompilation);
	}

	// Методы для удаления и получения подборок
	@Transactional
	@Override
	public void deleteCompilation(Long compId) {
		Compilation compilation = getCompFromRepo(compId);
		repository.deleteById(compId);
	}

	@Override
	public List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size) {
		Pageable pageRequest = PageRequest.of(from / size, size);
		List<Compilation> compilations;
		if (pinned != null) {
			compilations = repository.findByPinned(pinned, pageRequest);
		} else {
			compilations = repository.findAll(pageRequest).getContent();
		}
		return CompilationMapper.INSTANCE.mapListCompilations(compilations);
	}

	@Override
	public CompilationDtoResponse getCompilation(Long compId) {
		Compilation compilation = getCompFromRepo(compId);
		return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(compilation);
	}

	// Вспомогательные методы
	private Compilation getCompFromRepo(Long compId) {
		return repository.findById(compId)
				.orElseThrow(() -> new NotFoundException("Подборка с id = " + compId + " не существует"));
	}

	private List<Event> getEventsForCompilation(List<Long> eventIds) {
		return eventIds == null || eventIds.isEmpty() ? new ArrayList<>() : eventRepository.findAllById(eventIds);
	}

	private void updateCompilationFields(Compilation compilation, CompilationDtoUpdate compilationDto) {
		if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
			compilation.setEvents(eventRepository.findAllById(compilationDto.getEvents()));
		}
		if (compilationDto.getPinned() != null) {
			compilation.setPinned(compilationDto.getPinned());
		}
		if (compilationDto.getTitle() != null) {
			compilation.setTitle(compilationDto.getTitle());
		}
	}
}