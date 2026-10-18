package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvInventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryID")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Builder.Default
    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;
}