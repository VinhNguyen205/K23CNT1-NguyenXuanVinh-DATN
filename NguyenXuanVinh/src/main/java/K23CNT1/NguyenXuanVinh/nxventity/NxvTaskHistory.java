package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvTaskHistory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvTaskHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryID")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TaskID", nullable = false)
    private NxvTasks task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "ActionType", nullable = false, length = 20)
    private String actionType;

    @Builder.Default
    @Column(name = "ActionAt", nullable = false)
    private LocalDateTime actionAt = LocalDateTime.now();
}