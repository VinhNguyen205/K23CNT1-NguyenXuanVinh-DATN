package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvBillingTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvBillingTransactionsRepository extends JpaRepository<NxvBillingTransactions, Long> {
    List<NxvBillingTransactions> findByUser_UserIdOrderByConfirmedAtDesc(Long userId);
    List<NxvBillingTransactions> findTop100ByOrderByConfirmedAtDesc();
    Optional<NxvBillingTransactions> findByOrder_OrderId(Long orderId);
}