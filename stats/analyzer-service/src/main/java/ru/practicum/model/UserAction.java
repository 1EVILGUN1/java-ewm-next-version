package ru.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class UserAction {
    @Id
    private Long id;
    private Long userId;
    private Long eventId;
    private ActionType actionType;
    private Instant actionTime;
}
