package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NxvSystemAdminDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AdminDashboardSummaryDto {
        private long totalUsers;
        private long lockedUsers;
        private long deadUsers;
        private long totalCharacters;
        private long totalTasks;
        private long activePendingTasks;
        private long completedTasks;
        private long failedOverdueTasks;
        private long totalParties;
        private long totalPets;
        private long totalGachaPulls;
        private long activeMarketListings;
        private long totalMarketTransactions;
        private long pendingReports;
        private long totalRevivalOrdersPaid;
        private long totalRevenueVnd;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AdminUserRowDto {
        private Long userId;
        private String username;
        private String email;
        private Integer gender;
        private Integer role;
        private String roleLabel;
        private Boolean isEmailVerified;
        private Boolean isLocked;
        private Boolean isDead;
        private Integer loginFailCount;
        private String characterName;
        private Integer currentLevel;
        private Integer currentHp;
        private Integer maxHp;
        private Long gold;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangeUserRoleRequest {
        @NotNull(message = "role (0=User, 1=Moderator, 2=SuperAdmin) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private Integer role;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertItemRequest {
        @NotBlank(message = "TÃªn váº­t pháº©m khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String itemName;

        @NotNull(message = "Loáº¡i váº­t pháº©m (0=Equip, 1=Consumable, 2=Avatar, 3=Frame, 4=PetEgg, 5=Title) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private Integer itemType;

        private String imageUrl;
        private Integer equipSlotId;
        private Integer buffMaxHp;
        private Integer buffMaxMp;
        private BigDecimal buffExpBonus;
        private BigDecimal buffGoldBonus;
        private BigDecimal buffDeadlineDmgReduce;
        private Integer healHp;
        private Integer sellPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertGachaBannerRequest {
        @NotBlank(message = "TÃªn Banner khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String bannerName;

        private String bannerImageUrl;

        @NotNull(message = "GiÃ¡ má»—i láº§n má»Ÿ (costPerPull) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Min(value = 1, message = "GiÃ¡ má»Ÿ tá»‘i thiá»ƒu lÃ  1 Gold")
        private Integer costPerPull;

        @NotNull(message = "Má»‘c báº£o hiá»ƒm Pity (pityLimit) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Min(value = 1, message = "Má»‘c Pity tá»‘i thiá»ƒu lÃ  1")
        private Integer pityLimit;

        @Builder.Default
        private Boolean isActive = true;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertDropRateRequest {
        @NotNull(message = "bannerId khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private Integer bannerId;

        @NotNull(message = "itemId khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private Integer itemId;

        @NotNull(message = "Tá»· lá»‡ rá»›t (dropRate, VD: 0.050000) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private BigDecimal dropRate;

        @Builder.Default
        private Boolean isFeatured = false;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DifficultyPenaltyConfigDto {
        private Integer easyPenaltyHp;
        private Integer normalPenaltyHp;
        private Integer hardPenaltyHp;
        private Integer easyRewardExp;
        private Integer normalRewardExp;
        private Integer hardRewardExp;
        private Integer easyRewardGold;
        private Integer normalRewardGold;
        private Integer hardRewardGold;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendNotificationRequest {
        private Long targetUserId; // null = Global Notification day den toan bo Client
        @NotBlank(message = "TiÃªu Ä‘á» thÃ´ng bÃ¡o khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String title;
        @NotBlank(message = "Ná»™i dung thÃ´ng bÃ¡o khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String content;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationDto {
        private Long notificationId;
        private Long userId;
        private Boolean isGlobal;
        private String title;
        private String content;
        private String notificationType;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateUserReportRequest {
        private Long targetUserId;
        @NotBlank(message = "Loáº¡i bÃ¡o cÃ¡o (BUG, CHEAT, ABUSE, PAYMENT...) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String reportType;
        @NotBlank(message = "MÃ´ táº£ chi tiáº¿t khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResolveReportRequest {
        @NotNull(message = "Tráº¡ng thÃ¡i xá»­ lÃ½ (1=Resolved, 2=Rejected) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private Integer status;
        private String adminNote;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserReportDto {
        private Long reportId;
        private Long reporterUserId;
        private String reporterUsername;
        private Long targetUserId;
        private String targetUsername;
        private String reportType;
        private String description;
        private Integer status; // 0=Pending, 1=Resolved, 2=Rejected
        private String statusLabel;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertAiConfigRequest {
        @NotBlank(message = "Provider (GEMINI / OPENAI) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String provider;
        @NotBlank(message = "ModelName (VD: gemini-2.5-flash) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String modelName;
        @NotBlank(message = "APIKey khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String apiKeyEncrypted;
        private String systemPrompt;
        @Builder.Default
        private Boolean isActive = true;
    }
}