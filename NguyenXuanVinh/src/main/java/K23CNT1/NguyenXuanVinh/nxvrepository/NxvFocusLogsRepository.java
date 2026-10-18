package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvFocusLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NxvFocusLogsRepository extends JpaRepository<NxvFocusLogs, Long> {
    List<NxvFocusLogs> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_CompleteFocusSession @UserID = :userId, @DurationMinutes = :durationMinutes", nativeQuery = true)
    void executeCompleteFocusSessionSp(@Param("userId") Long userId, @Param("durationMinutes") Integer durationMinutes);
}