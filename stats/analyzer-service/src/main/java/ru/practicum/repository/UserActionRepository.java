package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.UserAction;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    List<UserAction> findAllByUserId(Long userId);

    List<UserAction> findAllByEventId(Long eventId);
}
