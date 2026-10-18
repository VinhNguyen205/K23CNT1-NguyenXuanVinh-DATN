package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvFocusLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvFocusLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FocusLogID")
    private Long focusLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "DurationMinutes", nullable = false)
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}