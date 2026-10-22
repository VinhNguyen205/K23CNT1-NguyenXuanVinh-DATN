package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvSkillUsageLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvSkillUsageLogsRepository extends JpaRepository<NxvSkillUsageLogs, Long> {
    List<NxvSkillUsageLogs> findTop100ByUser_UserIdOrderByUsedAtDesc(Long userId);
}