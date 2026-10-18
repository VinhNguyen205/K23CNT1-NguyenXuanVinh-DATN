package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "NxvVw_UserTotalBuffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvVwUserTotalBuffs {
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "TotalBuffMaxHP")
    private Integer totalBuffMaxHp;

    @Column(name = "TotalBuffMaxMP")
    private Integer totalBuffMaxMp;

    @Column(name = "TotalEXPBonus")
    private BigDecimal totalExpBonus;

    @Column(name = "TotalGoldBonus")
    private BigDecimal totalGoldBonus;

    @Column(name = "TotalDmgReduce")
    private BigDecimal totalDmgReduce;
}