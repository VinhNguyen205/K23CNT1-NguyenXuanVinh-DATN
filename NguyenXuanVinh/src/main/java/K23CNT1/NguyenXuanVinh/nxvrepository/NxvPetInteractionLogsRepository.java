package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvPetInteractionLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvPetInteractionLogsRepository extends JpaRepository<NxvPetInteractionLogs, Long> {
    List<NxvPetInteractionLogs> findTop50ByUserPet_UserPetIdOrderByCreatedAtDesc(Long userPetId);
    List<NxvPetInteractionLogs> findTop100ByUserPet_User_UserIdOrderByCreatedAtDesc(Long userId);
}