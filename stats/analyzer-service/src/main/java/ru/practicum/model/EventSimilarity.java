package ru.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EventSimilarity {
    @Id
    private Long id;
    private Long eventA;
    private Long eventB;
    private Double score;
    private Instant eventTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() && !(o instanceof HibernateProxy)) return false;
        EventSimilarity that = o instanceof HibernateProxy
                ? (EventSimilarity) ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation()
                : (EventSimilarity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }
}