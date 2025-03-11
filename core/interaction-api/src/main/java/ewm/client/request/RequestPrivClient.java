package ewm.client.request;

import ewm.dto.request.RequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "request-priv-client")
public interface RequestPrivClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    RequestDto create(@PathVariable Long userId,
                      @RequestParam Long eventId);

    @PatchMapping("/{requestId}/cancel")
    RequestDto cancelRequest(@PathVariable Long userId,
                             @PathVariable Long requestId);

    @GetMapping
    List<RequestDto> getRequests(@PathVariable Long userId);
}
