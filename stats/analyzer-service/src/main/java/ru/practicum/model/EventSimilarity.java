package ru.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class EventSimilarity {
    @Id
    private Long id;
    private Long eventA;
    private Long eventB;
    private Double score;
    private Instant eventTime;
}
