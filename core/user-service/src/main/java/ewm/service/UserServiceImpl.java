package ewm.service;

import ewm.dto.user.UserDto;
import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.mapper.UserMapper;
import ewm.model.User;
import ewm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей с параметрами: ids={}, from={}, size={}", ids, from, size);
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);
        List<UserDto> result;
        if (ids == null || ids.isEmpty()) {
            result = UserMapper.mapToUserDto(repository.findAll(pageRequest));
        } else {
            result = UserMapper.mapToUserDto(repository.findAllById(ids));
        }
        log.info("Успешно получено {} пользователей", result.size());
        return result;
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя с данными: {}", userDto);
        if (repository.getByEmail(userDto.getEmail()).isPresent()) {
            log.warn("Попытка создать пользователя с уже существующим email: {}", userDto.getEmail());
            throw new ExistException("Такой email уже есть");
        }
        User user = UserMapper.mapToUser(userDto);
        log.info("Создан user --> {}", user);
        UserDto result = UserMapper.mapToUserDto(repository.save(user));
        log.info("Пользователь успешно создан с id: {}", result.getId());
        return result;
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с userId: {}", userId);
        getUserFromRepo(userId);
        repository.deleteById(userId);
        log.info("Удален user с id --> {}", userId);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по userId: {}", userId);
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            log.warn("Пользователь с userId: {} не найден", userId);
            throw new NotFoundException("Пользователя с id = " + userId.toString() + " не существует");
        }
        UserDto result = UserMapper.mapToUserDto(user.get());
        log.info("Пользователь с userId: {} успешно получен", userId);
        return result;
    }

    private User getUserFromRepo(Long userId) {
        log.debug("Поиск пользователя с userId: {}", userId);
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            log.warn("Пользователь с userId: {} не найден в репозитории", userId);
            throw new NotFoundException("Пользователя с id = " + userId.toString() + " не существует");
        }
        return user.get();
    }
}