package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvGachaHistory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvGachaHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GachaHistoryID")
    private Long gachaHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BannerID", nullable = false)
    private NxvGachaBanners banner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Builder.Default
    @Column(name = "IsPity", nullable = false)
    private Boolean isPity = false;

    @Builder.Default
    @Column(name = "GoldSpent", nullable = false)
    private Integer goldSpent = 0;

    @Builder.Default
    @Column(name = "PulledAt", nullable = false)
    private LocalDateTime pulledAt = LocalDateTime.now();
}