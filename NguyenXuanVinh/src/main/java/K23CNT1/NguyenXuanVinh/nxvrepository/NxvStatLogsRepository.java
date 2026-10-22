package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvStatLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvStatLogsRepository extends JpaRepository<NxvStatLogs, Long> {
    List<NxvStatLogs> findTop100ByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<NxvStatLogs> findTop100ByUser_UserIdAndLogTypeIgnoreCaseOrderByCreatedAtDesc(Long userId, String logType);
}