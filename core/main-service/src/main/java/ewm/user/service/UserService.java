package ewm.user.service;

import ewm.dto.user.UserDto;
import ewm.model.user.User;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    void delete(Long userId);

    User getUserFromRepo(Long userId);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);
}
