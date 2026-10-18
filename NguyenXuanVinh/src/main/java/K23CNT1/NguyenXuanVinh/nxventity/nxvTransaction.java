package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions") // Đảm bảo tên bảng khớp với SQL
@Data // Sinh Getter, Setter, toString...
@NoArgsConstructor // Constructor không tham số (Bắt buộc cho Hibernate)
@AllArgsConstructor // Constructor full tham số
@Builder // Hỗ trợ tạo đối tượng nhanh (Design Pattern Builder)
public class nxvTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionID")
    private Integer transactionId;

    // Quan hệ nhiều-một với User (Bắt buộc phải có người thực hiện giao dịch)
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private nxvUser user;

    // Số tiền (Dùng BigDecimal để tính tiền chính xác)
    @Column(name = "Amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    // Loại giao dịch: DEPOSIT (Nạp), BUY_BOX (Mua), SELL_BACK (Bán lại), ORDER_PAYMENT (Thanh toán đơn)
    @Column(name = "TransactionType", nullable = false, length = 50)
    private String transactionType;

    // Mô tả chi tiết: "Nạp tiền qua Momo", "Mua hộp ABC"...
    @Column(name = "Description", length = 255)
    private String description;

    // Ngày giao dịch
    @Column(name = "TransactionDate")
    private LocalDateTime transactionDate;

    // Tự động gán ngày giờ hiện tại trước khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
    }
}