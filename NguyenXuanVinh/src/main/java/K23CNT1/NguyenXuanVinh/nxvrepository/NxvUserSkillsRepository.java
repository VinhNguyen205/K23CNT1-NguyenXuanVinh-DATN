package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserSkills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserSkillsRepository extends JpaRepository<NxvUserSkills, Long> {
    List<NxvUserSkills> findByUser_UserId(Long userId);
    Optional<NxvUserSkills> findByUser_UserIdAndSkill_SkillId(Long userId, Integer skillId);
}