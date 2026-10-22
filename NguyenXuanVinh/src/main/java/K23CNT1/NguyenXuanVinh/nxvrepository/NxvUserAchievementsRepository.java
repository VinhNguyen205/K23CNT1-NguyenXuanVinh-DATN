package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserAchievements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvUserAchievementsRepository extends JpaRepository<NxvUserAchievements, Long> {
    List<NxvUserAchievements> findByUser_UserIdOrderByUnlockedAtDesc(Long userId);
    boolean existsByUser_UserIdAndAchievement_AchievementId(Long userId, Integer achievementId);
    boolean existsByUser_UserIdAndAchievement_Code(Long userId, String code);
}