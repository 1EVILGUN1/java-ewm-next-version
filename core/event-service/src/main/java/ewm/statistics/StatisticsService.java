package ewm.statistics;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    void saveStats(HttpServletRequest request);

    Map<Long, Long> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}
