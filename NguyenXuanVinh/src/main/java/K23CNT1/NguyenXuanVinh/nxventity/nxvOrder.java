package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Orders") // Tên bảng trong SQL Server
@Data // Tự động sinh getter/setter/toString/hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nxvOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Integer orderId; // Đặt là orderId để dễ gọi (vd: order.orderId)

    // Quan hệ Many-to-One với bảng Users
    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private nxvUser user;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate;

    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;

    @Column(name = "OrderStatus")
    private String orderStatus;

    // --- CÁC TRƯỜNG THÔNG TIN GIAO HÀNG ---

    @Column(name = "ReceiverName")
    private String receiverName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "ShippingAddress")
    private String shippingAddress;

    @Column(name = "PaymentMethod")
    private String paymentMethod; // VD: WALLET, COD, VNPAY

    @Column(name = "PaymentStatus")
    private String paymentStatus; // VD: PAID, UNPAID

    @Column(name = "DeliveryDate")
    private LocalDateTime deliveryDate;

    // [QUAN TRỌNG] Trường này để sửa lỗi Thymeleaf (order.note)
    @Column(name = "Note")
    private String note;

    // --- HÀM TỰ ĐỘNG CHẠY TRƯỚC KHI LƯU VÀO DB ---
    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        if (this.orderStatus == null || this.orderStatus.isEmpty()) {
            this.orderStatus = "PENDING";
        }
        if (this.paymentStatus == null || this.paymentStatus.isEmpty()) {
            this.paymentStatus = "UNPAID";
        }
    }
}