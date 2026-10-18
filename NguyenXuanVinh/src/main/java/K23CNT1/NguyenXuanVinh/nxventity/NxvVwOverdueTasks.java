package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "NxvVw_OverdueTasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvVwOverdueTasks {
    @Id
    @Column(name = "TaskID")
    private Long taskId;

    @Column(name = "UserID")
    private Long userId;

    @Column(name = "PartyTaskID")
    private Long partyTaskId;

    @Column(name = "Title")
    private String title;

    @Column(name = "Difficulty")
    private Integer difficulty;

    @Column(name = "PenaltyHP")
    private Integer penaltyHp;

    @Column(name = "DeadlineAt")
    private LocalDateTime deadlineAt;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "IsShielded")
    private Boolean isShielded;
}