package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvGachaDropRates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvGachaDropRates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DropRateID")
    private Integer dropRateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BannerID", nullable = false)
    private NxvGachaBanners banner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Column(name = "DropRate", nullable = false, precision = 8, scale = 6)
    private BigDecimal dropRate;

    @Builder.Default
    @Column(name = "IsFeatured", nullable = false)
    private Boolean isFeatured = false;
}