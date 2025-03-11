package ewm.statistic;

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
    private final StatsClient client;

    @Override
    public void save(HttpServletRequest request) {
        log.info("Сохранение статистики запроса: IP = {}, URI = {}", request.getRemoteAddr(), request.getRequestURI());
        EndpointHitDTO hitDTO = EndpointHitDTO.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        client.saveHit(hitDTO);
        log.info("Статистика запроса успешно сохранена: {}", hitDTO);
    }

    @Override
    public Map<Long, Long> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        log.info("Получение статистики просмотров с {} по {} для URI: {}", start, end, uris);
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDTO> stats;
        List<?> list;

        StatsRequestDTO requestDTO = new StatsRequestDTO(
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                uris,
                true);

        ResponseEntity<Object> response;
        try {
            response = client.getStats(requestDTO);
        } catch (Exception e) {
            log.error("Ошибка при запросе статистики для URI {}: {}", uris, e.getMessage());
            throw e;
        }

        if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
            log.error("Ошибка получения статистики, код ответа: {}, URI: {}", response.getStatusCode(), uris);
            throw new HttpServerErrorException(response.getStatusCode(),
                    String.format("Ошибка получения статистики по url --> %s", uris));
        }

        if (response.getBody() instanceof List<?>) {
            list = (List<?>) response.getBody();
        } else {
            log.error("Неверный формат ответа сервера статистики для URI: {}", uris);
            throw new ClassCastException("Данные с сервера статистики не могут быть извлечены");
        }

        if (list.isEmpty()) {
            log.info("Статистика пуста для URI: {}, возвращаются нулевые просмотры", uris);
            return uris.stream().map(this::getEventIdFromUri)
                    .collect(Collectors.toMap(Function.identity(), s -> 0L));
        } else {
            stats = list.stream()
                    .map(e -> mapper.convertValue(e, ViewStatsDTO.class))
                    .collect(Collectors.toList());
            log.info("Получены данные статистики: {}", stats);
            Map<Long, Long> result = stats.stream()
                    .collect(Collectors.toMap(viewStats -> getEventIdFromUri(viewStats.getUri()),
                            ViewStatsDTO::getHits));
            log.info("Статистика успешно обработана, результат: {}", result);
            return result;
        }
    }

    private Long getEventIdFromUri(String uri) {
        log.debug("Извлечение ID события из URI: {}", uri);
        String[] parts = uri.split("/");
        Long eventId = Long.parseLong(parts[parts.length - 1]);
        log.debug("Извлечен ID события: {} из URI: {}", eventId, uri);
        return eventId;
    }
}