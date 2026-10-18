package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvSubTasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvSubTasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubTaskID")
    private Long subTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpicQuestID", nullable = false)
    private NxvEpicQuests epicQuest;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Builder.Default
    @Column(name = "IsDone", nullable = false)
    private Boolean isDone = false;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}