package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvAchievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvAchievements {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AchievementID")
    private Integer achievementId;

    @Column(name = "Code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "AchievementName", nullable = false, length = 150)
    private String achievementName;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "IconUrl", length = 500)
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RewardTitleID")
    private NxvTitles rewardTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RewardItemID")
    private NxvItems rewardItem;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;
}