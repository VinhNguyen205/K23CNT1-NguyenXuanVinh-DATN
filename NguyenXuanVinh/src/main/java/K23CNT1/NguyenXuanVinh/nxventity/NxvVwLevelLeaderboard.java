package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "NxvVw_LevelLeaderboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvVwLevelLeaderboard {
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "CharacterName")
    private String characterName;

    @Column(name = "CurrentLevel")
    private Integer currentLevel;

    @Column(name = "CurrentEXP")
    private Long currentExp;

    @Column(name = "Gold")
    private Long gold;
}