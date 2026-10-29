package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvUserSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserSessionsRepository extends JpaRepository<NxvUserSessions, Long> {
    List<NxvUserSessions> findByUser_UserIdOrderByLastActiveAtDesc(Long userId);
    Optional<NxvUserSessions> findByDeviceTokenAndIsActiveTrue(String deviceToken);

    /** Dùng cho JWT filter: nạp luôn User để không bị LazyInitializationException (filter chạy ngoài OpenEntityManagerInView). */
    @Query("select s from NxvUserSessions s join fetch s.user where s.deviceToken = :token and s.isActive = true")
    Optional<NxvUserSessions> findActiveByTokenWithUser(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("update NxvUserSessions s set s.lastActiveAt = :now where s.sessionId = :id")
    void touch(@Param("id") Long sessionId, @Param("now") LocalDateTime now);

    /** Đăng xuất / thu hồi tất cả thiết bị của một user (đổi mật khẩu, bị khóa...). */
    @Modifying
    @Transactional
    @Query("update NxvUserSessions s set s.isActive = false where s.user.userId = :userId and s.isActive = true")
    int deactivateAllByUserId(@Param("userId") Long userId);
    Optional<NxvUserSessions> findBySessionIdAndUser_UserId(Long sessionId, Long userId);
}