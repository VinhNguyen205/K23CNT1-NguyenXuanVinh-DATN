package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvOrder;
import K23CNT1.NguyenXuanVinh.nxventity.nxvOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvOrderDetailRepository extends JpaRepository<nxvOrderDetail, Integer> {
    // Lấy chi tiết của 1 đơn hàng
    List<nxvOrderDetail> findByOrder(nxvOrder order);
}