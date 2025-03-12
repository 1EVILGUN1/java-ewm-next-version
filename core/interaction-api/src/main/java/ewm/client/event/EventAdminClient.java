package ewm.client.event;

import ewm.dto.event.AdminGetEventRequestDto;
import ewm.dto.event.EventDto;
import ewm.dto.event.UpdateEventDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "event-admin-client")
public interface EventAdminClient {

    @GetMapping
    List<EventDto> adminGetEvents(AdminGetEventRequestDto requestParams);

    @PatchMapping("/{eventId}")
    EventDto adminChangeEvent(@PathVariable Long eventId,
                              @RequestBody @Valid UpdateEventDto eventDto);
}
