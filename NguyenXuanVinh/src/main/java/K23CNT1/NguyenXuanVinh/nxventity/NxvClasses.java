package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvClasses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvClasses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassID")
    private Integer classId;

    @Column(name = "ClassName", nullable = false, length = 100)
    private String className;

    @Builder.Default
    @Column(name = "PassiveEXPBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveExpBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "PassiveGoldBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveGoldBonus = BigDecimal.ZERO;
}