package ewm.compilation.model;

import ewm.event.model.Event;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "compilations_events",
            joinColumns = {@JoinColumn(name = "id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")}
    )
    @ToString.Exclude
    private List<Event> events;

    private Boolean pinned;

    private String title;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() && !(o instanceof HibernateProxy)) return false;
        Compilation that = o instanceof HibernateProxy
                ? (Compilation) ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation()
                : (Compilation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }
}