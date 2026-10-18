package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserSessionsRepository extends JpaRepository<NxvUserSessions, Long> {
    List<NxvUserSessions> findByUser_UserIdOrderByLastActiveAtDesc(Long userId);
    Optional<NxvUserSessions> findByDeviceTokenAndIsActiveTrue(String deviceToken);
    Optional<NxvUserSessions> findBySessionIdAndUser_UserId(Long sessionId, Long userId);
}