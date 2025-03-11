package ewm.stats;

import ewm.client.BaseClient;
import ewm.dto.EndpointHitDTO;
import ewm.dto.StatsRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";
    private static final String PARAM_SEPARATOR = "&";
    private static final String QUERY_START = "?";

    @Autowired
    public StatsClient(@Value("${stats.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build());
    }

    // Сохраняет информацию о новом запросе (hit)
    public ResponseEntity<Object> saveHit(EndpointHitDTO requestDto) {
        return post(HIT_ENDPOINT, requestDto);
    }

    // Получает статистику по запросам с учетом параметров
    public ResponseEntity<Object> getStats(StatsRequestDTO statsRequest) {
        Map<String, Object> parameters = createStatsParameters(statsRequest);
        String fullPath = STATS_ENDPOINT + buildQueryString(statsRequest);
        return get(fullPath, parameters);
    }

    // Создает параметры для GET-запроса статистики
    private Map<String, Object> createStatsParameters(StatsRequestDTO statsRequest) {
        return Map.of(
                "start", statsRequest.getStart(),
                "end", statsRequest.getEnd(),
                "unique", statsRequest.getUnique()
        );
    }

    // Формирует строку запроса (query string) из DTO
    private String buildQueryString(StatsRequestDTO statsRequest) {
        String urisAsString = String.join(",", statsRequest.getUris());
        StringBuilder queryBuilder = new StringBuilder(QUERY_START);

        appendParameter(queryBuilder, "start", statsRequest.getStart());
        queryBuilder.append(PARAM_SEPARATOR);
        appendParameter(queryBuilder, "end", statsRequest.getEnd());
        queryBuilder.append(PARAM_SEPARATOR);
        appendParameter(queryBuilder, "uris", urisAsString);
        queryBuilder.append(PARAM_SEPARATOR);
        appendParameter(queryBuilder, "unique", statsRequest.getUnique());

        return queryBuilder.toString();
    }

    // Добавляет ключ и значение в строку запроса
    private void appendParameter(StringBuilder builder, String key, Object value) {
        builder.append(key).append("=").append(value);
    }
}