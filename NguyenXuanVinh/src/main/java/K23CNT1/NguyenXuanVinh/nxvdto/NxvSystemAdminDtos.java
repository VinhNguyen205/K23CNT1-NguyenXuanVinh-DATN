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
        @NotNull(message = "role (0=User, 1=Moderator, 2=SuperAdmin) không được để trống")
        private Integer role;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertItemRequest {
        @NotBlank(message = "Tên vật phẩm không được để trống")
        private String itemName;

        @NotNull(message = "Loại vật phẩm (0=Equip, 1=Consumable, 2=Avatar, 3=Frame, 4=PetEgg, 5=Title) không được để trống")
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
        @NotBlank(message = "Tên Banner không được để trống")
        private String bannerName;

        private String bannerImageUrl;

        @NotNull(message = "Giá mỗi lần mở (costPerPull) không được để trống")
        @Min(value = 1, message = "Giá mở tối thiểu là 1 Gold")
        private Integer costPerPull;

        @NotNull(message = "Mốc bảo hiểm Pity (pityLimit) không được để trống")
        @Min(value = 1, message = "Mốc Pity tối thiểu là 1")
        private Integer pityLimit;

        @Builder.Default
        private Boolean isActive = true;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertDropRateRequest {
        @NotNull(message = "bannerId không được để trống")
        private Integer bannerId;

        @NotNull(message = "itemId không được để trống")
        private Integer itemId;

        @NotNull(message = "Tỷ lệ rớt (dropRate, VD: 0.050000) không được để trống")
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
        @NotBlank(message = "Tiêu đề thông báo không được để trống")
        private String title;
        @NotBlank(message = "Nội dung thông báo không được để trống")
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
        @NotBlank(message = "Loại báo cáo (BUG, CHEAT, ABUSE, PAYMENT...) không được để trống")
        private String reportType;
        @NotBlank(message = "Mô tả chi tiết không được để trống")
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResolveReportRequest {
        @NotNull(message = "Trạng thái xử lý (1=Resolved, 2=Rejected) không được để trống")
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
        @NotBlank(message = "Provider (GEMINI / OPENAI) không được để trống")
        private String provider;
        @NotBlank(message = "ModelName (VD: gemini-2.5-flash) không được để trống")
        private String modelName;
        @NotBlank(message = "APIKey không được để trống")
        private String apiKeyEncrypted;
        private String systemPrompt;
        @Builder.Default
        private Boolean isActive = true;
    }
}