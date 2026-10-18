package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "NxvVw_TaskLeaderboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvVwTaskLeaderboard {
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "CharacterName")
    private String characterName;

    @Column(name = "TotalTasksDone")
    private Integer totalTasksDone;
}