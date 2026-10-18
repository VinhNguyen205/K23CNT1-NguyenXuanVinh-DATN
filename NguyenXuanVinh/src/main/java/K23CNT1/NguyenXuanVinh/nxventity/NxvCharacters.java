package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvCharacters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvCharacters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CharacterID")
    private Long characterId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    private Users user;

    @Column(name = "CharacterName", nullable = false, length = 100)
    private String characterName;

    @Column(name = "AvatarUrl", length = 500)
    private String avatarUrl;

    @Column(name = "AvatarFrameUrl", length = 500)
    private String avatarFrameUrl;

    @Builder.Default
    @Column(name = "CurrentLevel", nullable = false)
    private Integer currentLevel = 1;

    @Builder.Default
    @Column(name = "CurrentEXP", nullable = false)
    private Long currentExp = 0L;

    @Builder.Default
    @Column(name = "EXPToNextLevel", nullable = false)
    private Long expToNextLevel = 100L;

    @Builder.Default
    @Column(name = "BaseMaxHP", nullable = false)
    private Integer baseMaxHp = 100;

    @Builder.Default
    @Column(name = "BaseMaxMP", nullable = false)
    private Integer baseMaxMp = 50;

    @Builder.Default
    @Column(name = "MaxHP", nullable = false)
    private Integer maxHp = 100;

    @Builder.Default
    @Column(name = "CurrentHP", nullable = false)
    private Integer currentHp = 100;

    @Builder.Default
    @Column(name = "MaxMP", nullable = false)
    private Integer maxMp = 50;

    @Builder.Default
    @Column(name = "CurrentMP", nullable = false)
    private Integer currentMp = 50;

    @Builder.Default
    @Column(name = "Gold", nullable = false)
    private Long gold = 0L;

    @Builder.Default
    @Column(name = "SkillPoints", nullable = false)
    private Integer skillPoints = 0;

    @Builder.Default
    @Column(name = "IsShieldActive", nullable = false)
    private Boolean isShieldActive = false;

    @Builder.Default
    @Column(name = "TotalTasksDone", nullable = false)
    private Integer totalTasksDone = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CurrentClassID")
    private NxvClasses currentClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CurrentTitleID")
    private NxvTitles currentTitle;

    @Builder.Default
    @Column(name = "StreakDays", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "LastCheckInDate")
    private LocalDate lastCheckInDate;

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}