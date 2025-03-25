package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionRepository repository;

    public void saveActionType(UserActionAvro userActionAvro) {
        UserAction userAction = new UserAction();
        userAction.setUserId(userAction.getUserId());
        userAction.setEventId(userAction.getEventId());
        userAction.setActionType(ActionType.valueOf(userActionAvro.getActionType().name()));
        userAction.setActionTime(userActionAvro.getTimestamp());

        repository.save(userAction);
    }

    public List<UserAction> getUserActionByUserId(Long userId) {
        return repository.findAllByUserId(userId);
    }

    public List<UserAction> getUserActionByEventId(Long eventId) {
        return repository.findAllByEventId(eventId);
    }
}
