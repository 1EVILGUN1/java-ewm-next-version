package ewm.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import ewm.dto.EndpointHitDTO;
import ewm.dto.StatsRequestDTO;
import ewm.dto.ViewStatsDTO;
import ewm.stats.StatsClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final StatsClient statsClient;

    @Override
    public void saveStats(HttpServletRequest request) {
        statsClient.saveHit(EndpointHitDTO.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build());
    }

    @Override
    public Map<Long, Long> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {

        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDTO> stats;
        List<?> list;

        StatsRequestDTO requestDTO = new StatsRequestDTO(
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                uris,
                true);

        ResponseEntity<Object> response = statsClient.getStats(requestDTO);

        if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
            throw new HttpServerErrorException(response.getStatusCode(),
                    String.format("Ошибка получения статистики по url --> %s", uris));
        }

        if (response.getBody() instanceof List<?>) {
            list = (List<?>) response.getBody();
        } else {
            throw new ClassCastException("Данные с сервера статистики не могут быть извлечены");
        }
        if (list.isEmpty()) {
            return uris.stream().map(this::getEventIdFromUri)
                    .collect(Collectors.toMap(Function.identity(), s -> 0L));
        } else {
            stats = list.stream()
                    .map(e -> mapper.convertValue(e, ViewStatsDTO.class))
                    .collect(Collectors.toList());
            log.info("Данные статистики --> {}", stats);
            return stats.stream()
                    .collect(Collectors.toMap(ViewStats -> getEventIdFromUri(ViewStats.getUri()),
                            ViewStatsDTO::getHits));
        }
    }

    private Long getEventIdFromUri(String uri) {
        String[] parts = uri.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
}
