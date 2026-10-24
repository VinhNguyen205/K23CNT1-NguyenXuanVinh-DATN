package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvFriendships;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvFriendshipsRepository extends JpaRepository<NxvFriendships, Long> {
    Optional<NxvFriendships> findByRequester_UserIdAndAddressee_UserId(Long requesterId, Long addresseeId);
    List<NxvFriendships> findByRequester_UserIdOrAddressee_UserIdOrderByCreatedAtDesc(Long requesterId, Long addresseeId);
}