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
public class UserAction {
    @Id
    private Long id;
    private Long userId;
    private Long eventId;
    private ActionType actionType;
    private Instant actionTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() && !(o instanceof HibernateProxy)) return false;
        UserAction that = o instanceof HibernateProxy
                ? (UserAction) ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation()
                : (UserAction) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }
}