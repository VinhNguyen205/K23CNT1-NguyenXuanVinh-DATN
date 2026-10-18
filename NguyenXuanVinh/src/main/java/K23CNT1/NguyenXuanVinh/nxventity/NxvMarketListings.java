package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvMarketListings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvMarketListings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ListingID")
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SellerUserID", nullable = false)
    private Users sellerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Builder.Default
    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "PricePerUnit", nullable = false)
    private Integer pricePerUnit;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "TaxRate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate = new BigDecimal("0.0500");

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "SoldAt")
    private LocalDateTime soldAt;
}