package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvOtpTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NxvOtpTokensRepository extends JpaRepository<NxvOtpTokens, Long> {
    Optional<NxvOtpTokens> findTopByUser_UserIdAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(Long userId, Integer tokenType);
}