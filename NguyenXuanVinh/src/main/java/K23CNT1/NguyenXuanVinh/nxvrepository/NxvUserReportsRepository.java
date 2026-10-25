package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvUserReportsRepository extends JpaRepository<NxvUserReports, Long> {
    List<NxvUserReports> findTop100ByOrderByCreatedAtDesc();
    List<NxvUserReports> findByStatusOrderByCreatedAtDesc(Integer status);
    List<NxvUserReports> findByReporterUser_UserIdOrderByCreatedAtDesc(Long reporterUserId);
}