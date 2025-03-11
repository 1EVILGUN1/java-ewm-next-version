package ewm.stats.service;

import ewm.dto.EndpointHitDTO;
import ewm.dto.EndpointHitResponseDto;
import ewm.dto.StatsRequestDTO;
import ewm.dto.ViewStatsDTO;

import java.util.List;

public interface HitService {
    EndpointHitResponseDto create(EndpointHitDTO endpointHitDTO);

    List<ViewStatsDTO> getAll(StatsRequestDTO statsRequestDTO);
}
