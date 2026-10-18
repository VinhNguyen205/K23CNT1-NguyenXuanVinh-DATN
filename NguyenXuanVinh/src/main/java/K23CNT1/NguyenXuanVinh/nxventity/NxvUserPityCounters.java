package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserPityCounters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserPityCounters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PityID")
    private Long pityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BannerID", nullable = false)
    private NxvGachaBanners banner;

    @Builder.Default
    @Column(name = "PullCount", nullable = false)
    private Integer pullCount = 0;

    @Builder.Default
    @Column(name = "TotalPulls", nullable = false)
    private Integer totalPulls = 0;
}