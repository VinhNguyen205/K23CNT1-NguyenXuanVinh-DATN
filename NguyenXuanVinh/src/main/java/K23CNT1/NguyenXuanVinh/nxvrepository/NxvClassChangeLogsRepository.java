package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvClassChangeLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvClassChangeLogsRepository extends JpaRepository<NxvClassChangeLogs, Long> {
    List<NxvClassChangeLogs> findTop50ByUser_UserIdOrderByChangedAtDesc(Long userId);
}