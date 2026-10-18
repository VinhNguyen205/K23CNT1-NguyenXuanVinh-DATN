package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface NxvTasksRepository extends JpaRepository<NxvTasks, Long> {
    List<NxvTasks> findByUser_UserIdOrderBySortOrderAscCreatedAtDesc(Long userId);

    @Query(value = "EXEC NxvSp_CompleteTask @TaskID = :taskId, @UserID = :userId", nativeQuery = true)
    Map<String, Object> executeCompleteTaskSp(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_CronDailyDeadlineCheck", nativeQuery = true)
    void executeCronDailyDeadlineCheckSp();
}