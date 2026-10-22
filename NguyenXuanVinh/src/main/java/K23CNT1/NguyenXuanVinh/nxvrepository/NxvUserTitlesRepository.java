package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserTitles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserTitlesRepository extends JpaRepository<NxvUserTitles, Long> {
    List<NxvUserTitles> findByUser_UserIdOrderByUnlockedAtDesc(Long userId);
    boolean existsByUser_UserIdAndTitle_TitleId(Long userId, Integer titleId);
    Optional<NxvUserTitles> findByUser_UserIdAndTitle_TitleId(Long userId, Integer titleId);
}