package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserTagMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserTagMasteryRepository extends JpaRepository<NxvUserTagMastery, Long> {
    List<NxvUserTagMastery> findByUser_UserIdOrderByTaskCountDesc(Long userId);
    Optional<NxvUserTagMastery> findByUser_UserIdAndTag_TagId(Long userId, Integer tagId);
}