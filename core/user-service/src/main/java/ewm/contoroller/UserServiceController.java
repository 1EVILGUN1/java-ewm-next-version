package ewm.contoroller;

import ewm.client.UserClient;
import ewm.dto.user.UserDto;
import ewm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserServiceController implements UserClient {
    private final UserService service;

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по userId: {}", userId);
        UserDto result = service.getUserById(userId);
        log.info("Пользователь с userId: {} успешно получен", userId);
        return result;
    }
}