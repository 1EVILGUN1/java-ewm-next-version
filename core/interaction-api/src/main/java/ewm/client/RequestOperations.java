package ewm.client;

import ewm.dto.request.RequestDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RequestOperations {
    @GetMapping("request/event/{eventId}")
    List<RequestDto> getRequestsByEventId(@PathVariable Long eventId);

    @GetMapping("request/find")
    List<RequestDto> findAllById(@RequestParam List<Long> ids);

    @PatchMapping("request")
    RequestDto updateRequest(@RequestBody RequestDto requestDto);

    @PostMapping("/request/all")
    List<RequestDto> updateAllRequest(@RequestBody List<RequestDto> requestDtoList);
}
