package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserPityCounters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserPityCountersRepository extends JpaRepository<NxvUserPityCounters, Long> {
    Optional<NxvUserPityCounters> findByUser_UserIdAndBanner_BannerId(Long userId, Integer bannerId);
    List<NxvUserPityCounters> findByUser_UserId(Long userId);
}