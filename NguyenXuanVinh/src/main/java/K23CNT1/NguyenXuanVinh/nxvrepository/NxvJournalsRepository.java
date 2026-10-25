package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvJournals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvJournalsRepository extends JpaRepository<NxvJournals, Long> {
    List<NxvJournals> findTop50ByUser_UserIdOrderByCreatedAtDesc(Long userId);
}