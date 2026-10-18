package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "BoxItems")
public class nxvBoxItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ItemID")
    private Integer itemId;

    @ManyToOne
    @JoinColumn(name = "BoxID")
    private nxvBlindBox blindBox;

    @Column(name = "ItemName", nullable = false)
    private String itemName;

    @Column(name = "ImageURL")
    private String imageUrl;

    @Column(name = "RarityLevel") // S, A, B, C, D
    private String rarityLevel;

    @Column(name = "Probability")
    private Double probability;

    @Column(name = "MarketValue", precision = 18, scale = 2)
    private BigDecimal marketValue;

    @Column(name = "StockQuantity")
    private Integer stockQuantity;

    @Column(name = "IsHidden")
    private Boolean isHidden;

    @Column(name = "IsPityReward")
    private Boolean isPityReward;

    @PrePersist
    protected void onCreate() {
        if (stockQuantity == null) stockQuantity = 0;
        if (isHidden == null) isHidden = false; // Mặc định là hiện (hoặc true nếu là item bí mật)
        if (isPityReward == null) isPityReward = false;
        if (marketValue == null) marketValue = BigDecimal.ZERO;
    }
}