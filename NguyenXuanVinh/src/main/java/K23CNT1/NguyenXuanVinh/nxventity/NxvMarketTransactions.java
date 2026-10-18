package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvMarketTransactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvMarketTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MarketTxID")
    private Long marketTxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ListingID", nullable = false)
    private NxvMarketListings listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BuyerUserID", nullable = false)
    private Users buyerUser;

    @Column(name = "TotalPrice", nullable = false)
    private Long totalPrice;

    @Builder.Default
    @Column(name = "TxAt", nullable = false)
    private LocalDateTime txAt = LocalDateTime.now();
}