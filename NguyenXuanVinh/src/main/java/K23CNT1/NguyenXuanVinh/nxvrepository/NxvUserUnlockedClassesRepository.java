package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserUnlockedClasses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvUserUnlockedClassesRepository extends JpaRepository<NxvUserUnlockedClasses, Long> {
    List<NxvUserUnlockedClasses> findByUser_UserIdOrderByUnlockedAtDesc(Long userId);
    boolean existsByUser_UserIdAndNxvClass_ClassId(Long userId, Integer classId);
}