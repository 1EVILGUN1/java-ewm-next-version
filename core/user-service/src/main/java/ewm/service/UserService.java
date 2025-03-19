package ewm.service;

import ewm.dto.user.UserDto;

import java.util.List;

// Управление пользователями
public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto createUser(UserDto userDto);

    void deleteUser(Long userId);

    UserDto getUserById(Long userId);
}
