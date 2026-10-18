package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvTransaction;
import K23CNT1.NguyenXuanVinh.nxvdto.nxvTopUserDTO; // Đảm bảo import DTO này
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface nxvTransactionRepository extends JpaRepository<nxvTransaction, Integer> {

    // --- 1. FIX LỖI: sumAmountByType ---
    // Tính tổng tiền theo 1 loại giao dịch cụ thể (VD: "DEPOSIT")
    @Query("SELECT SUM(t.amount) FROM nxvTransaction t WHERE t.transactionType = ?1")
    BigDecimal sumAmountByType(String transactionType);

    // --- 2. Hỗ trợ tìm Top Đại Gia (Dùng cho Dashboard) ---
    // Yêu cầu: Phải có DTO nxvTopUserDTO
    @Query("SELECT new K23CNT1.NguyenXuanVinh.nxvdto.nxvTopUserDTO(t.user, SUM(t.amount)) " +
            "FROM nxvTransaction t " +
            "WHERE t.transactionType IN ('DEPOSIT', 'ADMIN_DEPOSIT') " +
            "GROUP BY t.user " +
            "ORDER BY SUM(t.amount) DESC")
    List<nxvTopUserDTO> findTopDepositors(Pageable pageable);
}