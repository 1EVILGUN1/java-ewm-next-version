package ewm.user.controller;

import ewm.client.user.UserAdminClient;
import ewm.dto.user.UserDto;
import ewm.user.service.UserService;
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
public class UserController implements UserAdminClient {
    private final UserService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto userDto) {
        log.info("Получен запрос на создание пользователя: {}", userDto);
        UserDto result = service.create(userDto);
        log.info("Пользователь успешно создан: {}", result);
        return result;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя с ID: {}", userId);
        service.delete(userId);
        log.info("Пользователь с ID {} успешно удален", userId);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение списка пользователей с параметрами: ids = {}, from = {}, size = {}", ids, from, size);
        List<UserDto> result = service.getUsers(ids, from, size);
        log.info("Получено {} пользователей", result.size());
        return result;
    }
}