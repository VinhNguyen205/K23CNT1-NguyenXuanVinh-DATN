package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvGachaBanners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvGachaBanners {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BannerID")
    private Integer bannerId;

    @Builder.Default
    @Column(name = "BannerName", nullable = false, length = 150)
    private String bannerName = "Há»™p MÃ¹ BÃ­ áº¨n";

    @Column(name = "BannerImageUrl", length = 500)
    private String bannerImageUrl;

    @Builder.Default
    @Column(name = "CostPerPull", nullable = false)
    private Integer costPerPull = 100;

    @Builder.Default
    @Column(name = "PityLimit", nullable = false)
    private Integer pityLimit = 50;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}