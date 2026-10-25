package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvNotificationsRepository extends JpaRepository<NxvNotifications, Long> {
    @Query("SELECT n FROM NxvNotifications n WHERE n.user.userId = :userId OR n.user IS NULL ORDER BY n.createdAt DESC")
    List<NxvNotifications> findMyAndGlobalNotifications(@Param("userId") Long userId);

    List<NxvNotifications> findTop100ByOrderByCreatedAtDesc();
}