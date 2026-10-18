package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ItemID")
    private Integer itemId;

    @Column(name = "ItemName", nullable = false, length = 200)
    private String itemName;

    @Column(name = "ItemType", nullable = false)
    private Integer itemType;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EquipSlotID")
    private NxvEquipmentSlots equipSlot;

    @Builder.Default
    @Column(name = "BuffMaxHP", nullable = false)
    private Integer buffMaxHp = 0;

    @Builder.Default
    @Column(name = "BuffMaxMP", nullable = false)
    private Integer buffMaxMp = 0;

    @Builder.Default
    @Column(name = "BuffEXPBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal buffExpBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "BuffGoldBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal buffGoldBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "BuffDeadlineDmgReduce", nullable = false, precision = 5, scale = 4)
    private BigDecimal buffDeadlineDmgReduce = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "HealHP", nullable = false)
    private Integer healHp = 0;

    @Builder.Default
    @Column(name = "SellPrice", nullable = false)
    private Integer sellPrice = 0;
}