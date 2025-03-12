package ewm.stats;

import ewm.client.BaseClient;
import ewm.dto.EndpointHitDTO;
import ewm.dto.StatsRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";
    private static final String PARAM_SEPARATOR = "&";
    private static final String QUERY_START = "?";

    @Autowired
    public StatsClient(@Value("${stats.server.url}") String serverUrl, RestTemplate restTemplate) {
        super(restTemplate);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(serverUrl));
    }

    public ResponseEntity<Object> saveHit(EndpointHitDTO requestDto) {
        return post(HIT_ENDPOINT, requestDto);
    }

    public ResponseEntity<Object> getStats(StatsRequestDTO statsRequest) {
        Map<String, Object> parameters = createStatsParameters(statsRequest);
        String fullPath = STATS_ENDPOINT + buildQueryString(statsRequest);
        return get(fullPath, parameters);
    }

    private Map<String, Object> createStatsParameters(StatsRequestDTO statsRequest) {
        return Map.of(
                "start", statsRequest.getStart(),
                "end", statsRequest.getEnd(),
                "unique", statsRequest.getUnique()
        );
    }

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

    private void appendParameter(StringBuilder builder, String key, Object value) {
        builder.append(key).append("=").append(value);
    }
}

@Configuration
class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}