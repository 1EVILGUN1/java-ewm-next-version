package ewm.user.service;

import ewm.dto.user.UserDto;
import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.model.user.User;
import ewm.user.mapper.UserMapper;
import ewm.user.repository.UserRepository;
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

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);
        Optional<User> existingUser = repository.getByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            log.error("Попытка создать пользователя с уже существующим email: {}", userDto.getEmail());
            throw new ExistException("Такой email уже есть");
        }
        User user = UserMapper.mapToUser(userDto);
        User savedUser = repository.save(user);
        UserDto result = UserMapper.mapToUserDto(savedUser);
        log.info("Пользователь успешно создан: {}", result);
        return result;
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        getUserFromRepo(userId);
        repository.deleteById(userId);
        log.info("Пользователь с ID {} успешно удален", userId);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение списка пользователей с параметрами: ids = {}, from = {}, size = {}", ids, from, size);
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = repository.findAll(pageRequest).getContent();
        } else {
            users = repository.findAllById(ids);
        }
        List<UserDto> result = UserMapper.mapToUserDto(users);
        log.info("Получено {} пользователей", result.size());
        return result;
    }

    @Override
    public User getUserFromRepo(Long userId) {
        log.debug("Поиск пользователя с ID: {}", userId);
        Optional<User> user = repository.findById(userId);
        if (user.isEmpty()) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
        log.debug("Пользователь с ID {} найден", userId);
        return user.get();
    }
}