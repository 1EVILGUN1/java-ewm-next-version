package ewm.stats.repository;

import ewm.stats.model.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query(
            value = "select h.app as app, h.uri as uri, count(h.*) as hits from Hit as h " +
                    "where h.timestamp >= ?1 and h.timestamp <= ?2 and (?3 = false or h.uri in ?4) " +
                    "group by h.app, h.uri " +
                    "order by count(h.*) desc", nativeQuery = true
    )
    List<ResponseHit> findAllBetweenDatesAndByUri(LocalDateTime from, LocalDateTime to, Boolean isUriListExist, List<String> uriList);

    @Query(value = "select v.app as app, v.uri as uri, count(v.*) as hits " +
                   "from ( " +
                   "select distinct h.ip, h.app, h.uri " +
                   "from hit as h " +
                   "where timestamp >= ?1 and timestamp <= ?2 and (?3 = false or h.uri in ?4)" +
                   ") AS v " +
                   "group by v.app, v.uri " +
                   "order by count(v.*) desc ", nativeQuery = true)
    List<ResponseHit> findAllUniqueBetweenDatesAndByUri(LocalDateTime start, LocalDateTime end, Boolean isUriListExist, List<String> uriList);

    interface ResponseHit {
        String getApp();

        String getUri();

        Long getHits();
    }
}
