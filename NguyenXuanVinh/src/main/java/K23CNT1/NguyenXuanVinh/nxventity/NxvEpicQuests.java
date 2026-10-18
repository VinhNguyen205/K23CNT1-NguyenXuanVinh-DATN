package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvEpicQuests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvEpicQuests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EpicQuestID")
    private Long epicQuestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyID")
    private NxvParties party;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Builder.Default
    @Column(name = "IsGPAQuest", nullable = false)
    private Boolean isGpaQuest = false;

    @Column(name = "TargetGPA", precision = 4, scale = 2)
    private BigDecimal targetGpa;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}