package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserPets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserPetsRepository extends JpaRepository<NxvUserPets, Long> {
    List<NxvUserPets> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    Optional<NxvUserPets> findFirstByUser_UserIdAndIsActiveTrueAndIsHibernatingFalseAndStageGreaterThanEqual(Long userId, Integer minStage);
}