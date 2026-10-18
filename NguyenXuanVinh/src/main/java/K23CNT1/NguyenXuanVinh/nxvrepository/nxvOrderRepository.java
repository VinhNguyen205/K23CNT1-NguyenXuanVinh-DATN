package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvOrder;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface nxvOrderRepository extends JpaRepository<nxvOrder, Integer> {

    // 1. Lấy tất cả đơn hàng (Mới nhất lên đầu)
    List<nxvOrder> findAllByOrderByOrderDateDesc();

    // 2. Lịch sử mua hàng của một User cụ thể (Cho trang cá nhân)
    List<nxvOrder> findByUserOrderByOrderDateDesc(nxvUser user);

    // 3. Tìm kiếm theo Trạng thái (Cho Admin Filter)
    List<nxvOrder> findByOrderStatusOrderByOrderDateDesc(String status);

    // 4. Tìm kiếm theo Từ khóa (Tên khách hoặc SĐT) - Không cần trạng thái
    List<nxvOrder> findByReceiverNameContainingOrPhoneNumberContainingOrderByOrderDateDesc(String name, String phone);

    // 5. [NÂNG CẤP] Tìm kiếm kết hợp: Trạng thái + Từ khóa (Tên hoặc SĐT)
    // Sử dụng @Query để đảm bảo logic (Status AND (Name OR Phone)) được thực thi đúng
    @Query("SELECT o FROM nxvOrder o WHERE o.orderStatus = :status AND (o.receiverName LIKE %:keyword% OR o.phoneNumber LIKE %:keyword%) ORDER BY o.orderDate DESC")
    List<nxvOrder> findByStatusAndKeyword(@Param("status") String status, @Param("keyword") String keyword);
}