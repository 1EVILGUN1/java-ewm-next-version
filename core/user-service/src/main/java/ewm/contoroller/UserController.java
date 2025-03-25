package ewm.contoroller;

import ewm.dto.user.UserDto;
import ewm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("admin/users")
public class UserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получить список пользователей по ids --> {}, from --> {}, size --> {}", ids, from, size);
        List<UserDto> result = service.getUsers(ids, from, size);
        log.info("Успешно получено {} пользователей", result.size());
        return result;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        log.info("Добавить пользователя user --> {}", userDto);
        UserDto result = service.createUser(userDto);
        log.info("Пользователь успешно добавлен с id: {}", result.getId());
        return result;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удалить пользователя по userId --> {}", userId);
        service.deleteUser(userId);
        log.info("Пользователь с userId: {} успешно удалён", userId);
    }
}