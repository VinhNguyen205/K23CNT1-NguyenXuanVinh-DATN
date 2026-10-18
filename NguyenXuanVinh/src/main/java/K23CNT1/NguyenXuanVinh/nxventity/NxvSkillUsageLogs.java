package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvSkillUsageLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvSkillUsageLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsageID")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SkillID", nullable = false)
    private NxvSkillDefinitions skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetTaskID")
    private NxvTasks targetTask;

    @Builder.Default
    @Column(name = "UsedAt", nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();
}