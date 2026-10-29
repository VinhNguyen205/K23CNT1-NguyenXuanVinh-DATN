package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class NxvCharacterDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CharacterDashboardDto {
        private Long characterId;
        private Long userId;
        private String username;
        private String characterName;
        private Integer gender;
        private Boolean isDead;

        // Static 2D Visuals (Khong paper-doll)
        private String avatarUrl;
        private String avatarFrameUrl;

        // Level & EXP
        private Integer currentLevel;
        private Long currentExp;
        private Long expToNextLevel;
        private Double expProgressPercent;

        // Base vs Effective HP/MP (Schema v2.2)
        private Integer baseMaxHp;
        private Integer maxHp;
        private Integer currentHp;
        private Integer baseMaxMp;
        private Integer maxMp;
        private Integer currentMp;

        // Economy & Skills
        private Long gold;
        private Integer skillPoints;
        private Boolean isShieldActive;
        private Integer totalTasksDone;
        private Integer streakDays;
        private LocalDate lastCheckInDate;

        // Class & Title
        private Integer currentClassId;
        private String currentClassName;
        private Integer currentTitleId;
        private String currentTitleName;
        private String currentTitleIcon;

        // Total Equipment Buffs from View NxvVw_UserTotalBuffs
        private Integer totalBuffMaxHp;
        private Integer totalBuffMaxMp;
        private BigDecimal totalExpBonus;
        private BigDecimal totalGoldBonus;
        private BigDecimal totalDmgReduce;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateCharacterNameRequest {
        @NotBlank(message = "Tên nhân vật không được để trống")
        private String characterName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangeAvatarRequest {
        private Integer itemId;
        private String avatarUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangeAvatarFrameRequest {
        private Integer itemId;
        private String avatarFrameUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EquipTitleRequest {
        private Integer titleId; // null = thao danh hieu
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TitleDto {
        private Integer titleId;
        private String titleName;
        private String titleIcon;
        private Boolean isExclusive;
        private Boolean isUnlocked;
        private Boolean isEquipped;
        private LocalDateTime unlockedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AchievementDto {
        private Integer achievementId;
        private String code;
        private String achievementName;
        private String description;
        private String iconUrl;
        private Integer rewardGold;
        private Integer rewardExp;
        private Integer rewardTitleId;
        private String rewardTitleName;
        private Integer rewardItemId;
        private Boolean isUnlocked;
        private LocalDateTime unlockedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatLogDto {
        private Long logId;
        private String logType;
        private Long delta;
        private String sourceType;
        private Long sourceId;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LevelStepDto {
        private Integer level;
        private Long expRequired;
        private Integer baseMaxHp;
        private Integer baseMaxMp;
        private Boolean isConfiguredInDb;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PublicProfileCardDto {
        private Long userId;
        private String username;
        private String characterName;
        private String avatarUrl;
        private String avatarFrameUrl;
        private Integer currentLevel;
        private Long gold;
        private Integer totalTasksDone;
        private Integer streakDays;
        private String className;
        private String titleName;
        private String titleIcon;
        private Integer unlockedAchievementsCount;
        private List<String> equippedItemNames;
        private List<String> badgeNames;
    }
}