package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserInventory")
@Data // Tự động sinh Getter, Setter, toString...
@NoArgsConstructor // Cần thiết cho JPA
@AllArgsConstructor // Cần thiết cho @Builder
@Builder // Để dùng được .builder() trong Service
public class nxvUserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryID")
    private Integer inventoryId;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private nxvUser user;

    @ManyToOne
    @JoinColumn(name = "ItemID")
    private nxvBoxItem boxItem;

    // Trường này giữ lại nếu bạn muốn dùng cho logic cũ
    @Column(name = "ObtainedDate")
    private LocalDateTime obtainedDate;

    // [QUAN TRỌNG] Thêm trường này để Repository sắp xếp (OrderByCreatedAtDesc) không bị lỗi
    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "Status") // IN_STORAGE, SOLD_BACK, REQUESTED_SHIP...
    private String status;

    @Column(name = "SoldPrice", precision = 18, scale = 2)
    private BigDecimal soldPrice;

    // Tự động set thời gian khi tạo mới
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.obtainedDate == null) {
            this.obtainedDate = now;
        }

        // Luôn đảm bảo CreatedAt có dữ liệu
        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.status == null) {
            this.status = "IN_STORAGE";
        }
    }
}