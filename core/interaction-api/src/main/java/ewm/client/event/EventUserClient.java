package ewm.client.event;

import ewm.dto.event.*;
import ewm.dto.request.RequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "event-user-client")
public interface EventUserClient {
    @GetMapping
    List<EventDto> getEvents(@PathVariable Long userId,
                             @RequestParam(name = "from", defaultValue = "0") Integer from,
                             @RequestParam(name = "size", defaultValue = "10") Integer size);

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    EventDto createEvent(@PathVariable Long userId,
                         @Valid @RequestBody CreateEventDto event);

    @GetMapping("/{id}")
    EventDto getEvent(@PathVariable Long userId,
                      @PathVariable Long id,
                      HttpServletRequest request);

    @PatchMapping("/{eventId}")
    EventDto updateEvent(@PathVariable Long userId,
                         @PathVariable Long eventId,
                         @Valid @RequestBody UpdateEventDto event);

    @GetMapping("/{eventId}/requests")
    List<RequestDto> getEventRequests(@PathVariable Long userId,
                                      @PathVariable Long eventId);

    @PatchMapping("/{eventId}/requests")
    EventRequestStatusUpdateResult changeStatusEventRequests(@PathVariable Long userId,
                                                             @PathVariable Long eventId,
                                                             @RequestBody
                                                             @Valid EventRequestStatusUpdateRequest request);
}
