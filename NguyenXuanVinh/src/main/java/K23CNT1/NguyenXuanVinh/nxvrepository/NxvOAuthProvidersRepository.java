package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvOAuthProviders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvOAuthProvidersRepository extends JpaRepository<NxvOAuthProviders, Long> {
    Optional<NxvOAuthProviders> findByProviderAndProviderUserId(String provider, String providerUserId);
    List<NxvOAuthProviders> findByUser_UserId(Long userId);
}