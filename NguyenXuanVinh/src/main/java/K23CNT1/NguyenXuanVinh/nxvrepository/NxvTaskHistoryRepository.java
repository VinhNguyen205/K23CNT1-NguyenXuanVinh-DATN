package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvTaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvTaskHistoryRepository extends JpaRepository<NxvTaskHistory, Long> {
    List<NxvTaskHistory> findTop100ByUser_UserIdOrderByActionAtDesc(Long userId);
    List<NxvTaskHistory> findByTask_TaskIdOrderByActionAtDesc(Long taskId);
}