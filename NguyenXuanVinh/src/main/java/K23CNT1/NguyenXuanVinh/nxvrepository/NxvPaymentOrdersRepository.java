package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvPaymentOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvPaymentOrdersRepository extends JpaRepository<NxvPaymentOrders, Long> {
    Optional<NxvPaymentOrders> findByOrderCode(String orderCode);
    List<NxvPaymentOrders> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<NxvPaymentOrders> findTop100ByOrderByCreatedAtDesc();
}