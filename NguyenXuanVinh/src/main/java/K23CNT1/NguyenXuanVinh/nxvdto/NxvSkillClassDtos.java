package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NxvSkillClassDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TagMasteryDto {
        private Integer tagId;
        private String tagName;
        private String colorHex;
        private Integer taskCount;
        private LocalDateTime updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClassInfoDto {
        private Integer classId;
        private String className;
        private BigDecimal passiveExpBonus;
        private BigDecimal passiveGoldBonus;
        private Boolean isUnlocked;
        private Boolean isCurrentClass;
        private Boolean isRecommended;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClassOverviewDto {
        private Long userId;
        private Integer currentClassId;
        private String currentClassName;
        private BigDecimal currentPassiveExpBonus;
        private BigDecimal currentPassiveGoldBonus;
        private Integer recommendedClassId;
        private String recommendedClassName;
        private String recommendationReason;
        private Long changeClassCostGold;
        private Long currentGold;
        private List<TagMasteryDto> tagMasteries;
        private List<ClassInfoDto> classes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangeClassRequest {
        @NotNull(message = "classId không được để trống")
        private Integer classId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClassChangeLogDto {
        private Long logId;
        private Integer fromClassId;
        private String fromClassName;
        private Integer toClassId;
        private String toClassName;
        private LocalDateTime changedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SkillNodeDto {
        private Integer skillId;
        private String skillName;
        private Integer skillType; // 0=Active, 1=Passive
        private String skillTypeLabel;
        private Integer mpCost;
        private Integer baseCooldownSeconds;
        private Integer effectiveCooldownSeconds;
        private Integer requiredLevel;
        private Integer maxSkillLevel;
        private Integer parentSkillId;
        private String parentSkillName;
        private String skillEffect;
        private Boolean isUnlocked;
        private Integer currentSkillLevel;
        private LocalDateTime cooldownEndsAt;
        private Long remainingCooldownSeconds;
        private Boolean canUnlock;
        private Boolean canUpgrade;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SkillTreeOverviewDto {
        private Long userId;
        private Integer characterLevel;
        private Integer availableSkillPoints;
        private Integer totalPointsSpent;
        private Integer currentMp;
        private Integer maxMp;
        private Long currentGold;
        private Long resetTreeCostGold;
        private Boolean isShieldActive;
        private List<SkillNodeDto> skills;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CastSkillRequest {
        private Long targetTaskId; // Bat buoc cho DASH_24H, tuy chon cho SHIELD_TASK
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SkillUsageLogDto {
        private Long usageId;
        private Integer skillId;
        private String skillName;
        private String skillEffect;
        private Long targetTaskId;
        private String targetTaskTitle;
        private LocalDateTime usedAt;
    }
}