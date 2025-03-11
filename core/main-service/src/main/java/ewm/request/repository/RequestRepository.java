package ewm.request.repository;

import ewm.model.request.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequester_IdAndEvent_id(Long userId, Long eventId);

    List<Request> findAllByRequester_Id(Long userId);

    List<Request> findAllByEvent_id(Long eventId);
}
