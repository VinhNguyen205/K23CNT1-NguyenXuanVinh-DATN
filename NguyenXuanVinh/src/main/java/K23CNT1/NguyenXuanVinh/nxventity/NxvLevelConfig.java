package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvLevelConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvLevelConfig {
    @Id
    @Column(name = "Level")
    private Integer level;

    @Column(name = "EXPRequired", nullable = false)
    private Long expRequired;

    @Builder.Default
    @Column(name = "MaxHP", nullable = false)
    private Integer maxHp = 100;

    @Builder.Default
    @Column(name = "MaxMP", nullable = false)
    private Integer maxMp = 50;
}