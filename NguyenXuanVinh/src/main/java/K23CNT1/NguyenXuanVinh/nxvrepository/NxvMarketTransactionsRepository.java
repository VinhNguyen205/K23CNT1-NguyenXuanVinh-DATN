package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvMarketTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvMarketTransactionsRepository extends JpaRepository<NxvMarketTransactions, Long> {
    List<NxvMarketTransactions> findTop100ByBuyerUser_UserIdOrderByTxAtDesc(Long buyerUserId);
    List<NxvMarketTransactions> findTop100ByListing_SellerUser_UserIdOrderByTxAtDesc(Long sellerUserId);
    List<NxvMarketTransactions> findTop100ByOrderByTxAtDesc();
}