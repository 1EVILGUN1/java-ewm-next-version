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
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
	private final CompilationRepository compilationRepository;
	private final EventRepository eventRepository;

	@Transactional
	@Override
	public CompilationDtoResponse createCompilation(CompilationDto compilationDto) {
		List<Event> events = compilationDto.getEvents() == null ?
				new ArrayList<>() : eventRepository.findAllById(compilationDto.getEvents());
		Compilation compilation = Compilation.builder()
				.events(events)
				.title(compilationDto.getTitle())
				.pinned(compilationDto.getPinned())
				.build();
		return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(compilationRepository.save(compilation));
	}

	@Transactional
	@Override
	public void deleteCompilation(Long compId) {
		getCompFromRepo(compId);
		compilationRepository.deleteById(compId);
	}

	@Transactional
	@Override
	public CompilationDtoResponse updateCompilation(Long compId, CompilationDtoUpdate compilationDto) {
		Compilation compilation = getCompFromRepo(compId);
		if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
			compilation.setEvents(eventRepository.findAllById(compilationDto.getEvents()));
		}
		if (compilationDto.getPinned() != null) compilation.setPinned(compilationDto.getPinned());
		if (compilationDto.getTitle() != null) compilation.setTitle(compilationDto.getTitle());
		return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(compilationRepository.save(compilation));
	}

	@Override
	public List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size) {
		int page = from / size;
		Pageable pageRequest = PageRequest.of(page, size);
		if (pinned != null)
			return CompilationMapper.INSTANCE.mapListCompilations(compilationRepository.findByPinned(pinned, pageRequest));
		return CompilationMapper.INSTANCE.mapListCompilations(compilationRepository.findAll(pageRequest).getContent());
	}

	@Override
	public CompilationDtoResponse getCompilation(Long compId) {
		return CompilationMapper.INSTANCE.compilationToCompilationDtoResponse(getCompFromRepo(compId));
	}

	private Compilation getCompFromRepo(Long compId) {
		Optional<Compilation> compilation = compilationRepository.findById(compId);
		if (compilation.isEmpty())
			throw new NotFoundException("Подборки с id = " + compId.toString() + " не существует");
		return compilation.get();
	}
}
