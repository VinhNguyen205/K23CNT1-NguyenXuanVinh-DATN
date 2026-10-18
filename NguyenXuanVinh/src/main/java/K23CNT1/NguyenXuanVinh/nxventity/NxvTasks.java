package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvTasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvTasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TaskID")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpicQuestID")
    private NxvEpicQuests epicQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyTaskID")
    private NxvTasks partyTask;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "DescriptionMarkdown", columnDefinition = "NVARCHAR(MAX)")
    private String descriptionMarkdown;

    @Column(name = "ColorLabel", length = 7)
    private String colorLabel;

    @Builder.Default
    @Column(name = "Difficulty", nullable = false)
    private Integer difficulty = 1;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "RewardMP", nullable = false)
    private Integer rewardMp = 0;

    @Builder.Default
    @Column(name = "PenaltyHP", nullable = false)
    private Integer penaltyHp = 0;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "SortOrder", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "DeadlineAt")
    private LocalDateTime deadlineAt;

    @Column(name = "ReminderBeforeMins")
    private Integer reminderBeforeMins;

    @Builder.Default
    @Column(name = "IsFrozen", nullable = false)
    private Boolean isFrozen = false;

    @Builder.Default
    @Column(name = "IsShielded", nullable = false)
    private Boolean isShielded = false;

    @Builder.Default
    @Column(name = "UndoAvailable", nullable = false)
    private Boolean undoAvailable = false;

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}