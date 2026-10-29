package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvPetSpecies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvPetSpecies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SpeciesID")
    private Integer speciesId;

    @Column(name = "SpeciesName", nullable = false, length = 100)
    private String speciesName;

    @Builder.Default
    @Column(name = "ElementType", nullable = false, length = 50)
    private String elementType = "Tự nhiên";

    @Column(name = "EggImageUrl", length = 500)
    private String eggImageUrl;

    @Column(name = "BabyImageUrl", length = 500)
    private String babyImageUrl;

    @Column(name = "AdultImageUrl", length = 500)
    private String adultImageUrl;

    @Builder.Default
    @Column(name = "EvolveDays", nullable = false)
    private Integer evolveDays = 14;

    @Builder.Default
    @Column(name = "PassiveGoldBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveGoldBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "PassiveDropRateBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveDropRateBonus = BigDecimal.ZERO;
}