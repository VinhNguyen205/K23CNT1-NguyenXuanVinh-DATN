package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvShipmentRequest;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface nxvShipmentRepository extends JpaRepository<nxvShipmentRequest, Integer> {

    // 1. ADMIN: Lấy tất cả vận đơn (Mới nhất lên đầu)
    // Dùng cho hàm getAllOrders() cũ hoặc khi vào trang Admin mà chưa filter
    List<nxvShipmentRequest> findAllByOrderByRequestDateDesc();

    // 2. USER: Lấy lịch sử vận đơn của một User cụ thể
    // Dùng cho trang /shipment/history của khách hàng
    List<nxvShipmentRequest> findByUserOrderByRequestDateDesc(nxvUser user);

    // 3. ADMIN: Tìm kiếm nâng cao (Kết hợp Lọc trạng thái + Tìm từ khóa)
    // Dùng cho thanh tìm kiếm bên Admin
    @Query("SELECT s FROM nxvShipmentRequest s WHERE " +
            "(:status IS NULL OR s.shipmentStatus = :status) AND " +
            "(:keyword IS NULL OR s.receiverName LIKE %:keyword% OR s.phoneNumber LIKE %:keyword%) " +
            "ORDER BY s.requestDate DESC")
    List<nxvShipmentRequest> searchShipments(@Param("status") String status, @Param("keyword") String keyword);
}