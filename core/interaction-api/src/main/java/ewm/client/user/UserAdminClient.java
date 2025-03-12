package ewm.client.user;

import ewm.dto.user.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-admin-client")
public interface UserAdminClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    UserDto create(@RequestBody @Valid UserDto userDto);

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    void delete(@PathVariable Long userId);

    @GetMapping
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                           @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size);
}
