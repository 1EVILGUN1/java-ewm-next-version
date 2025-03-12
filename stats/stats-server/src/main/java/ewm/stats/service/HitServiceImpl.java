package ewm.stats.service;

import ewm.dto.EndpointHitDTO;
import ewm.dto.EndpointHitResponseDto;
import ewm.dto.StatsRequestDTO;
import ewm.dto.ViewStatsDTO;
import ewm.stats.error.expection.BadRequestExceptions;
import ewm.stats.mapper.HitMapper;
import ewm.stats.model.Hit;
import ewm.stats.repository.HitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Реализация сервиса для работы с обращениями (hits) и статистикой.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String MISSING_DATES_ERROR = "You need to pass start and end dates";
    private static final String INVALID_DATE_RANGE_ERROR = "Start date must be before end date";

    private final HitMapper hitMapper;
    private final HitRepository hitRepository;

    /**
     * Создает новый hit на основе DTO и возвращает его в виде EndpointHitResponseDto.
     */
    @Override
    @Transactional
    public EndpointHitResponseDto create(EndpointHitDTO endpointHitDto) {
        Hit hit = buildHitFromDto(endpointHitDto);
        Hit savedHit = hitRepository.save(hit);
        return hitMapper.hitToEndpointHitResponseDto(savedHit);
    }

    /**
     * Получает статистику по обращениям за указанный период с учетом параметров.
     */
    @Override
    public List<ViewStatsDTO> getAll(StatsRequestDTO statsRequest) {
        validateDateRange(statsRequest);
        LocalDateTime start = parseDateTime(statsRequest.getStart());
        LocalDateTime end = parseDateTime(statsRequest.getEnd());
        boolean hasUris = !statsRequest.getUris().isEmpty();

        List<HitRepository.ResponseHit> hits = fetchHits(statsRequest, start, end, hasUris);
        return convertHitsToViewStatsDTO(hits);
    }

    // Создает объект Hit из DTO
    private Hit buildHitFromDto(EndpointHitDTO dto) {
        return Hit.builder()
                .ip(dto.getIp())
                .app(dto.getApp())
                .uri(dto.getUri())
                .timestamp(parseDateTime(dto.getTimestamp()))
                .build();
    }

    // Проверяет наличие и корректность дат в запросе
    private void validateDateRange(StatsRequestDTO statsRequest) {
        if (statsRequest.getStart() == null || statsRequest.getEnd() == null) {
            throw new BadRequestExceptions(MISSING_DATES_ERROR);
        }
        LocalDateTime start = parseDateTime(statsRequest.getStart());
        LocalDateTime end = parseDateTime(statsRequest.getEnd());
        if (start.isAfter(end)) {
            throw new BadRequestExceptions(INVALID_DATE_RANGE_ERROR);
        }
    }

    // Извлекает обращения из репозитория в зависимости от параметров
    private List<HitRepository.ResponseHit> fetchHits(StatsRequestDTO statsRequest, LocalDateTime start,
                                                      LocalDateTime end, boolean hasUris) {
        if (Boolean.TRUE.equals(statsRequest.getUnique())) {
            return hitRepository.findAllUniqueBetweenDatesAndByUri(start, end, hasUris, statsRequest.getUris());
        }
        return hitRepository.findAllBetweenDatesAndByUri(start, end, hasUris, statsRequest.getUris());
    }

    // Преобразует список обращений из репозитория в список DTO статистики
    private List<ViewStatsDTO> convertHitsToViewStatsDTO(List<HitRepository.ResponseHit> hits) {
        return hits.stream()
                .map(this::buildViewStatsDtoFromResponseHit)
                .toList();
    }

    // Создает объект ViewStatsDTO из ResponseHit
    private ViewStatsDTO buildViewStatsDtoFromResponseHit(HitRepository.ResponseHit responseHit) {
        return ViewStatsDTO.builder()
                .app(responseHit.getApp())
                .uri(responseHit.getUri())
                .hits(responseHit.getHits())
                .build();
    }

    // Парсит строку даты и времени в LocalDateTime
    private LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }
}