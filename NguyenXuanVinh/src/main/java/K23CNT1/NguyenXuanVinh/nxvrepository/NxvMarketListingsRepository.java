package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvMarketListings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvMarketListingsRepository extends JpaRepository<NxvMarketListings, Long> {
    List<NxvMarketListings> findByStatusOrderByCreatedAtDesc(Integer status);
    List<NxvMarketListings> findBySellerUser_UserIdOrderByCreatedAtDesc(Long sellerUserId);
    Optional<NxvMarketListings> findByListingIdAndSellerUser_UserId(Long listingId, Long sellerUserId);
}