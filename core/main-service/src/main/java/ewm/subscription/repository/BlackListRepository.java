package ewm.subscription.repository;

import ewm.model.subscription.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface BlackListRepository extends JpaRepository<BlackList, Long> {

    List<BlackList> findAllByUserId(Long userId);

    @Modifying
    @Query("delete from BlackList where userId = :userId and blackList = :blackListId")
    void deleteByUserIdAndBlockUser(Long userId, Long blackListId);

    @Query("select bl from BlackList as bl where bl.userId = :userId and bl.blackList = :blackListId")
    Optional<BlackList> findByUserIdAndBlockUser(Long userId, Long blackListId);
}
