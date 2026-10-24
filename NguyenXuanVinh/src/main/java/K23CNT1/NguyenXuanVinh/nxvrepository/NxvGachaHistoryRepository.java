package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvGachaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvGachaHistoryRepository extends JpaRepository<NxvGachaHistory, Long> {
    List<NxvGachaHistory> findTop100ByUser_UserIdOrderByPulledAtDesc(Long userId);
    List<NxvGachaHistory> findTop100ByOrderByPulledAtDesc();
}