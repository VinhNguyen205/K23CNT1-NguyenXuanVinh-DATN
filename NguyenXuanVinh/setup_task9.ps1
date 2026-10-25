# ==============================================================================
# TASKTALE / LIFERPG - TASK 9 AUTOMATED GENERATOR (MODULE 8: ADMIN & SYSTEM)
# Hoan thien Chuc nang #103 -> #115: Admin Overview Dashboard, Ban/Unban Users,
# Multi-level Role Management, Dynamic Gacha Rate & Pity Config, Item & Banner CRUD,
# Difficulty Penalty Config, Cronjob Monitor, Global Notifications, Server StatLogs,
# Tag Management, User Reports & AI Chatbot Config (122/122 Chuc nang Hoan tat!)
# ==============================================================================

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Write-JavaFile {
    param([string]$FilePath, [string]$Content)
    $dir = Split-Path -Parent $FilePath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    if (Test-Path $FilePath) {
        Remove-Item $FilePath -Force
    }
    [System.IO.File]::WriteAllText($FilePath, $Content, $utf8NoBom)
}

$javaRoot = Join-Path (Get-Location) "src\main\java"
$entityDirObj = Get-ChildItem -Path $javaRoot -Recurse -Directory -Filter "nxventity" | Select-Object -First 1
$baseDir = $entityDirObj.Parent.FullName
$basePkg = $baseDir.Substring($javaRoot.Length).TrimStart('\', '/') -replace '[\\/]', '.'

Write-Host "==> Dang sinh Module 8 (Task 9: Admin Dashboard, Gacha/Item Config, Reports, AI Config) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 8
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvNotificationsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvNotificationsRepository extends JpaRepository<NxvNotifications, Long> {
    @Query("SELECT n FROM NxvNotifications n WHERE n.user.userId = :userId OR n.user IS NULL ORDER BY n.createdAt DESC")
    List<NxvNotifications> findMyAndGlobalNotifications(@Param("userId") Long userId);

    List<NxvNotifications> findTop100ByOrderByCreatedAtDesc();
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserReportsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvUserReportsRepository extends JpaRepository<NxvUserReports, Long> {
    List<NxvUserReports> findTop100ByOrderByCreatedAtDesc();
    List<NxvUserReports> findByStatusOrderByCreatedAtDesc(Integer status);
    List<NxvUserReports> findByReporterUser_UserIdOrderByCreatedAtDesc(Long reporterUserId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvStatLogsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvStatLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvStatLogsRepository extends JpaRepository<NxvStatLogs, Long> {
    List<NxvStatLogs> findTop100ByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<NxvStatLogs> findTop100ByUser_UserIdAndLogTypeIgnoreCaseOrderByCreatedAtDesc(Long userId, String logType);
    List<NxvStatLogs> findTop200ByOrderByCreatedAtDesc();
    List<NxvStatLogs> findTop200ByLogTypeIgnoreCaseOrderByCreatedAtDesc(String logType);
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 8 (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvSystemAdminDtos.java" @"
package $basePkg.nxvdto;

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
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 8 (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvSystemAdminService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvInventoryDtos.ItemDetailDto;
import $basePkg.nxvdto.NxvSystemAdminDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvenums.NxvConstants;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvSystemAdminService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvTasksRepository tasksRepository;
    private final NxvPartiesRepository partiesRepository;
    private final NxvUserPetsRepository userPetsRepository;
    private final NxvItemsRepository itemsRepository;
    private final NxvEquipmentSlotsRepository equipmentSlotsRepository;
    private final NxvGachaBannersRepository gachaBannersRepository;
    private final NxvGachaDropRatesRepository gachaDropRatesRepository;
    private final NxvGachaHistoryRepository gachaHistoryRepository;
    private final NxvMarketListingsRepository marketListingsRepository;
    private final NxvMarketTransactionsRepository marketTransactionsRepository;
    private final NxvNotificationsRepository notificationsRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvTagsRepository tagsRepository;
    private final NxvUserReportsRepository userReportsRepository;
    private final NxvAIConfigRepository aiConfigRepository;
    private final NxvBillingTransactionsRepository billingTransactionsRepository;
    private final NxvWebhookLogsRepository webhookLogsRepository;
    private final NxvInventoryService inventoryService;
    private final NxvTaskQuestService taskQuestService;
    private final NxvPetIrlService petIrlService;

    // Cau hinh dong He so Phat HP & Thuong mac dinh (#108)
    private DifficultyPenaltyConfigDto difficultyConfig = DifficultyPenaltyConfigDto.builder()
            .easyPenaltyHp(10)
            .normalPenaltyHp(15)
            .hardPenaltyHp(25)
            .easyRewardExp(20)
            .normalRewardExp(35)
            .hardRewardExp(60)
            .easyRewardGold(15)
            .normalRewardGold(25)
            .hardRewardGold(40)
            .build();

    // =========================================================================
    // PHAN 1: DASHBOARD THONG KE, QUAN LY USER BAN/UNBAN & PHAN QUYEN (#103, #104, #113)
    // =========================================================================

    // Chuc nang #103: Dashboard thong ke tong quan toan he thong
    @Transactional(readOnly = true)
    public AdminDashboardSummaryDto getSystemDashboardSummary() {
        List<Users> allUsers = usersRepository.findAll();
        long lockedCount = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getIsLocked())).count();
        long deadCount = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getIsDead())).count();

        List<NxvTasks> allTasks = tasksRepository.findAll();
        long pendingTasks = allTasks.stream().filter(t -> t.getStatus() == NxvConstants.TASK_PENDING).count();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == NxvConstants.TASK_COMPLETED).count();
        long failedTasks = allTasks.stream().filter(t -> t.getStatus() == NxvConstants.TASK_FAILED).count();

        List<NxvBillingTransactions> billings = billingTransactionsRepository.findAll();
        long totalRevenueVnd = billings.stream()
                .mapToLong(b -> b.getAmountVnd() != null ? b.getAmountVnd() : 0L)
                .sum();

        long activeMarketCount = marketListingsRepository.findByStatusOrderByCreatedAtDesc(0).size();
        long pendingReportCount = userReportsRepository.findByStatusOrderByCreatedAtDesc(0).size();

        return AdminDashboardSummaryDto.builder()
                .totalUsers(allUsers.size())
                .lockedUsers(lockedCount)
                .deadUsers(deadCount)
                .totalCharacters(charactersRepository.count())
                .totalTasks(allTasks.size())
                .activePendingTasks(pendingTasks)
                .completedTasks(completedTasks)
                .failedOverdueTasks(failedTasks)
                .totalParties(partiesRepository.count())
                .totalPets(userPetsRepository.count())
                .totalGachaPulls(gachaHistoryRepository.count())
                .activeMarketListings(activeMarketCount)
                .totalMarketTransactions(marketTransactionsRepository.count())
                .pendingReports(pendingReportCount)
                .totalRevivalOrdersPaid(billings.size())
                .totalRevenueVnd(totalRevenueVnd)
                .build();
    }

    // Chuc nang #104: Xem danh sach & Ban/Unban tai khoan bang Users
    @Transactional(readOnly = true)
    public List<AdminUserRowDto> getAllUsersForAdmin() {
        return usersRepository.findAll().stream().map(u -> {
            NxvCharacters c = charactersRepository.findByUser_UserId(u.getUserId()).orElse(null);
            String rLabel = switch (u.getRole() != null ? u.getRole() : 0) {
                case 2 -> "SUPER_ADMIN (Quản trị tối cao)";
                case 1 -> "MODERATOR (Điều hành viên)";
                default -> "USER (Người chơi)";
            };
            return AdminUserRowDto.builder()
                    .userId(u.getUserId())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .gender(u.getGender())
                    .role(u.getRole())
                    .roleLabel(rLabel)
                    .isEmailVerified(u.getIsEmailVerified())
                    .isLocked(u.getIsLocked())
                    .isDead(u.getIsDead())
                    .loginFailCount(u.getLoginFailCount())
                    .characterName(c != null ? c.getCharacterName() : null)
                    .currentLevel(c != null ? c.getCurrentLevel() : 1)
                    .currentHp(c != null ? c.getCurrentHp() : 0)
                    .maxHp(c != null ? c.getMaxHp() : 100)
                    .gold(c != null ? c.getGold() : 0L)
                    .lastLoginAt(u.getLastLoginAt())
                    .createdAt(u.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public AdminUserRowDto toggleBanUser(Long targetUserId, boolean lock) {
        Users u = usersRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng ID: " + targetUserId));
        u.setIsLocked(lock);
        if (!lock) {
            u.setLoginFailCount(0);
        }
        u.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(u);
        return getAllUsersForAdmin().stream()
                .filter(row -> Objects.equals(row.getUserId(), targetUserId))
                .findFirst().orElseThrow();
    }

    // Chuc nang #113: Phan quyen Admin da cap (0=User, 1=Moderator, 2=SuperAdmin)
    @Transactional
    public AdminUserRowDto changeUserRole(Long targetUserId, ChangeUserRoleRequest req) {
        if (req.getRole() < 0 || req.getRole() > 2) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Role chỉ nhận giá trị: 0 (User), 1 (Moderator), 2 (SuperAdmin)!");
        }
        Users u = usersRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng ID: " + targetUserId));
        u.setRole(req.getRole());
        u.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(u);
        return getAllUsersForAdmin().stream()
                .filter(row -> Objects.equals(row.getUserId(), targetUserId))
                .findFirst().orElseThrow();
    }

    // =========================================================================
    // PHAN 2: QUAN LY VAT PHAM, BANNER HOP MU & TY LE GACHA / PITY (#105, #106, #107)
    // =========================================================================

    // Chuc nang #106: Quan ly danh muc Vat pham (Upload icon tinh 2D, setup chi so Buff)
    @Transactional
    public ItemDetailDto createCatalogItem(UpsertItemRequest req) {
        NxvEquipmentSlots slot = null;
        if (req.getEquipSlotId() != null) {
            slot = equipmentSlotsRepository.findById(req.getEquipSlotId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Slot trang bị ID: " + req.getEquipSlotId()));
        }

        NxvItems item = itemsRepository.save(NxvItems.builder()
                .itemName(req.getItemName().trim())
                .itemType(req.getItemType())
                .imageUrl(req.getImageUrl() != null ? req.getImageUrl().trim() : "/images/items/default_item_2d.png")
                .equipSlot(slot)
                .buffMaxHp(req.getBuffMaxHp() != null ? req.getBuffMaxHp() : 0)
                .buffMaxMp(req.getBuffMaxMp() != null ? req.getBuffMaxMp() : 0)
                .buffExpBonus(req.getBuffExpBonus() != null ? req.getBuffExpBonus() : BigDecimal.ZERO)
                .buffGoldBonus(req.getBuffGoldBonus() != null ? req.getBuffGoldBonus() : BigDecimal.ZERO)
                .buffDeadlineDmgReduce(req.getBuffDeadlineDmgReduce() != null ? req.getBuffDeadlineDmgReduce() : BigDecimal.ZERO)
                .healHp(req.getHealHp() != null ? req.getHealHp() : 0)
                .sellPrice(req.getSellPrice() != null ? req.getSellPrice() : 50)
                .build());

        return inventoryService.toItemDetailDto(item);
    }

    @Transactional
    public ItemDetailDto updateCatalogItem(Integer itemId, UpsertItemRequest req) {
        NxvItems item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy vật phẩm ID: " + itemId));

        if (req.getItemName() != null && !req.getItemName().isBlank()) item.setItemName(req.getItemName().trim());
        if (req.getItemType() != null) item.setItemType(req.getItemType());
        if (req.getImageUrl() != null) item.setImageUrl(req.getImageUrl().trim());
        if (req.getEquipSlotId() != null) {
            NxvEquipmentSlots slot = equipmentSlotsRepository.findById(req.getEquipSlotId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Slot trang bị ID: " + req.getEquipSlotId()));
            item.setEquipSlot(slot);
        }
        if (req.getBuffMaxHp() != null) item.setBuffMaxHp(req.getBuffMaxHp());
        if (req.getBuffMaxMp() != null) item.setBuffMaxMp(req.getBuffMaxMp());
        if (req.getBuffExpBonus() != null) item.setBuffExpBonus(req.getBuffExpBonus());
        if (req.getBuffGoldBonus() != null) item.setBuffGoldBonus(req.getBuffGoldBonus());
        if (req.getBuffDeadlineDmgReduce() != null) item.setBuffDeadlineDmgReduce(req.getBuffDeadlineDmgReduce());
        if (req.getHealHp() != null) item.setHealHp(req.getHealHp());
        if (req.getSellPrice() != null) item.setSellPrice(req.getSellPrice());

        return inventoryService.toItemDetailDto(itemsRepository.save(item));
    }

    // Chuc nang #107: Quan ly danh sach Hop mu (CRUD Banner, gan anh tinh cho Banner & Moc Pity)
    @Transactional
    public NxvGachaBanners createGachaBanner(UpsertGachaBannerRequest req) {
        return gachaBannersRepository.save(NxvGachaBanners.builder()
                .bannerName(req.getBannerName().trim())
                .bannerImageUrl(req.getBannerImageUrl() != null ? req.getBannerImageUrl().trim() : "/images/banners/default_banner_2d.png")
                .costPerPull(req.getCostPerPull())
                .pityLimit(req.getPityLimit())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build());
    }

    @Transactional
    public NxvGachaBanners updateGachaBanner(Integer bannerId, UpsertGachaBannerRequest req) {
        NxvGachaBanners banner = gachaBannersRepository.findById(bannerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Banner ID: " + bannerId));
        if (req.getBannerName() != null && !req.getBannerName().isBlank()) banner.setBannerName(req.getBannerName().trim());
        if (req.getBannerImageUrl() != null) banner.setBannerImageUrl(req.getBannerImageUrl().trim());
        if (req.getCostPerPull() != null) banner.setCostPerPull(req.getCostPerPull());
        if (req.getPityLimit() != null) banner.setPityLimit(req.getPityLimit());
        if (req.getIsActive() != null) banner.setIsActive(req.getIsActive());
        return gachaBannersRepository.save(banner);
    }

    // Chuc nang #105: Cau hinh dong thuat toan Gacha (Chinh sua Ty le rot do DropRate & Featured)
    @Transactional
    public Map<String, Object> upsertGachaDropRate(UpsertDropRateRequest req) {
        NxvGachaBanners banner = gachaBannersRepository.findById(req.getBannerId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Banner ID: " + req.getBannerId()));
        NxvItems item = itemsRepository.findById(req.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Vật phẩm ID: " + req.getItemId()));

        NxvGachaDropRates rateEntry = gachaDropRatesRepository.findByBanner_BannerIdOrderByDropRateAsc(req.getBannerId())
                .stream()
                .filter(dr -> Objects.equals(dr.getItem().getItemId(), req.getItemId()))
                .findFirst()
                .orElseGet(() -> NxvGachaDropRates.builder()
                        .banner(banner)
                        .item(item)
                        .build());

        rateEntry.setDropRate(req.getDropRate());
        rateEntry.setIsFeatured(Boolean.TRUE.equals(req.getIsFeatured()));
        rateEntry = gachaDropRatesRepository.save(rateEntry);

        return Map.of(
                "dropRateId", rateEntry.getDropRateId(),
                "bannerId", banner.getBannerId(),
                "bannerName", banner.getBannerName(),
                "itemId", item.getItemId(),
                "itemName", item.getItemName(),
                "dropRate", rateEntry.getDropRate(),
                "isFeatured", rateEntry.getIsFeatured()
        );
    }

    // =========================================================================
    // PHAN 3: CAU HINH HE SO PHAT, KIEM SOAT CRONJOB & THONG BAO GLOBAL (#108, #109, #110)
    // =========================================================================

    // Chuc nang #108: Cau hinh cac he so Phat (Luong HP bi tru) mac dinh cho tung do kho cua Task
    public DifficultyPenaltyConfigDto getDifficultyPenaltyConfig() {
        return difficultyConfig;
    }

    @Transactional
    public Map<String, Object> updateDifficultyPenaltyConfig(DifficultyPenaltyConfigDto req, boolean applyToExistingPendingTasks) {
        if (req.getEasyPenaltyHp() != null) difficultyConfig.setEasyPenaltyHp(req.getEasyPenaltyHp());
        if (req.getNormalPenaltyHp() != null) difficultyConfig.setNormalPenaltyHp(req.getNormalPenaltyHp());
        if (req.getHardPenaltyHp() != null) difficultyConfig.setHardPenaltyHp(req.getHardPenaltyHp());
        if (req.getEasyRewardExp() != null) difficultyConfig.setEasyRewardExp(req.getEasyRewardExp());
        if (req.getNormalRewardExp() != null) difficultyConfig.setNormalRewardExp(req.getNormalRewardExp());
        if (req.getHardRewardExp() != null) difficultyConfig.setHardRewardExp(req.getHardRewardExp());
        if (req.getEasyRewardGold() != null) difficultyConfig.setEasyRewardGold(req.getEasyRewardGold());
        if (req.getNormalRewardGold() != null) difficultyConfig.setNormalRewardGold(req.getNormalRewardGold());
        if (req.getHardRewardGold() != null) difficultyConfig.setHardRewardGold(req.getHardRewardGold());

        int updatedTasks = 0;
        if (applyToExistingPendingTasks) {
            for (NxvTasks t : tasksRepository.findAll()) {
                if (t.getStatus() == NxvConstants.TASK_PENDING) {
                    int d = t.getDifficulty() != null ? t.getDifficulty() : 1;
                    t.setPenaltyHp(d == 3 ? difficultyConfig.getHardPenaltyHp()
                            : d == 2 ? difficultyConfig.getNormalPenaltyHp() : difficultyConfig.getEasyPenaltyHp());
                    tasksRepository.save(t);
                    updatedTasks++;
                }
            }
        }
        return Map.of("config", difficultyConfig, "pendingTasksUpdated", updatedTasks);
    }

    // Chuc nang #109: Theo doi va kiem soat cac Cronjob dang chay ngam
    @Transactional
    public Map<String, Object> triggerAllSystemCronjobsManually() {
        Map<String, Object> deadlineCronResult = taskQuestService.executeNightlyDeadlineCronAndReminders();
        Map<String, Object> petHibernationResult = petIrlService.checkAndProcessPetHibernationCron();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("executedAt", LocalDateTime.now());
        res.put("deadlineCheckCron", deadlineCronResult);
        res.put("petHibernationCron", petHibernationResult);
        return res;
    }

    // Chuc nang #110: Gui thong bao Global Notification day den toan bo Client & Xem thong bao
    @Transactional
    public NotificationDto sendNotification(SendNotificationRequest req) {
        Users target = null;
        if (req.getTargetUserId() != null) {
            target = usersRepository.findById(req.getTargetUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người nhận ID: " + req.getTargetUserId()));
        }

        NxvNotifications n = notificationsRepository.save(NxvNotifications.builder()
                .user(target)
                .title(req.getTitle().trim())
                .content(req.getContent().trim())
                .notificationType(target == null ? "GLOBAL" : "SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return toNotificationDto(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getMyAndGlobalNotifications(Long userId) {
        return notificationsRepository.findMyAndGlobalNotifications(userId).stream()
                .map(this::toNotificationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markNotificationRead(Long notificationId) {
        NxvNotifications n = notificationsRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy thông báo ID: " + notificationId));
        n.setIsRead(true);
        return toNotificationDto(notificationsRepository.save(n));
    }

    // =========================================================================
    // PHAN 4: XEM LOG HE THONG, QUAN LY TAG, REPORT & AI CONFIG (#111, #112, #114, #115)
    // =========================================================================

    // Chuc nang #111: Xem Log he thong (Lich su nap/tru Vang, EXP, HP toan server de phat hien bug)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getGlobalServerStatLogs(String logType) {
        List<NxvStatLogs> logs = (logType != null && !logType.isBlank())
                ? statLogsRepository.findTop200ByLogTypeIgnoreCaseOrderByCreatedAtDesc(logType.trim())
                .stream().toList()
                : statLogsRepository.findTop200ByOrderByCreatedAtDesc();

        return logs.stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("logId", l.getLogId());
            m.put("userId", l.getUser().getUserId());
            m.put("username", l.getUser().getUsername());
            m.put("logType", l.getLogType());
            m.put("delta", l.getDelta());
            m.put("sourceType", l.getSourceType());
            m.put("sourceId", l.getSourceId());
            m.put("createdAt", l.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
    }

    // Chuc nang #112: Quan ly the Tag chuyen mon mac dinh cua he thong
    @Transactional
    public NxvTags updateSystemTag(Integer tagId, String tagName, String colorHex) {
        NxvTags tag = tagsRepository.findById(tagId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Tag ID: " + tagId));
        if (tagName != null && !tagName.isBlank()) tag.setTagName(tagName.trim());
        if (colorHex != null && !colorHex.isBlank()) tag.setColorHex(colorHex.trim());
        return tagsRepository.save(tag);
    }

    @Transactional
    public void deleteSystemTag(Integer tagId) {
        tagsRepository.deleteById(tagId);
    }

    // Chuc nang #114: Quan ly khieu nai / Report tu nguoi dung
    @Transactional
    public UserReportDto submitUserReport(Long reporterUserId, CreateUserReportRequest req) {
        Users reporter = usersRepository.findById(reporterUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người gửi báo cáo!"));
        Users target = null;
        if (req.getTargetUserId() != null) {
            target = usersRepository.findById(req.getTargetUserId()).orElse(null);
        }

        NxvUserReports r = userReportsRepository.save(NxvUserReports.builder()
                .reporterUser(reporter)
                .targetUser(target)
                .reportType(req.getReportType().trim().toUpperCase())
                .description(req.getDescription().trim())
                .status(0) // 0 = Pending
                .createdAt(LocalDateTime.now())
                .build());

        return toUserReportDto(r);
    }

    @Transactional(readOnly = true)
    public List<UserReportDto> getAllUserReports(Integer status) {
        List<NxvUserReports> list = (status != null)
                ? userReportsRepository.findByStatusOrderByCreatedAtDesc(status)
                : userReportsRepository.findTop100ByOrderByCreatedAtDesc();
        return list.stream().map(this::toUserReportDto).collect(Collectors.toList());
    }

    @Transactional
    public UserReportDto resolveUserReport(Long reportId, ResolveReportRequest req) {
        NxvUserReports r = userReportsRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy báo cáo/khiếu nại ID: " + reportId));
        r.setStatus(req.getStatus());
        r = userReportsRepository.save(r);

        notificationsRepository.save(NxvNotifications.builder()
                .user(r.getReporterUser())
                .title("Báo cáo #" + reportId + " đã được Admin xử lý")
                .content("Khiếu nại [" + r.getReportType() + "] của bạn đã được cập nhật trạng thái: "
                        + (req.getStatus() == 1 ? "ĐÃ GIẢI QUYẾT" : "TỪ CHỐI")
                        + (req.getAdminNote() != null ? ". Ghi chú Admin: " + req.getAdminNote() : ""))
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return toUserReportDto(r);
    }

    // Chuc nang #115: Quan ly cau hinh API ket noi AI Chatbot (Doi token/model/prompt)
    @Transactional(readOnly = true)
    public List<NxvAIConfig> getAllAiConfigs() {
        return aiConfigRepository.findAll();
    }

    @Transactional
    public NxvAIConfig upsertAiConfig(UpsertAiConfigRequest req) {
        NxvAIConfig cfg = aiConfigRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(NxvAIConfig::new);
        cfg.setProvider(req.getProvider().trim().toUpperCase());
        cfg.setModelName(req.getModelName().trim());
        cfg.setApiKeyEncrypted(req.getApiKeyEncrypted().trim());
        cfg.setSystemPrompt(req.getSystemPrompt());
        cfg.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        cfg.setUpdatedAt(LocalDateTime.now());
        return aiConfigRepository.save(cfg);
    }

    // Xem doi soat Billing & Webhook toan server cho Admin (#121)
    @Transactional(readOnly = true)
    public Map<String, Object> getAllBillingAndWebhookLogsForAdmin() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("billingTransactions", billingTransactionsRepository.findTop100ByOrderByConfirmedAtDesc().stream().map(tx -> Map.of(
                "billingTxId", tx.getBillingTxId(),
                "orderCode", tx.getOrder().getOrderCode(),
                "userId", tx.getUser().getUserId(),
                "username", tx.getUser().getUsername(),
                "amountVnd", tx.getAmountVnd(),
                "confirmedAt", tx.getConfirmedAt()
        )).collect(Collectors.toList()));
        res.put("webhookLogs", webhookLogsRepository.findTop100ByOrderByCreatedAtDesc());
        return res;
    }

    // --- Helpers ---
    private NotificationDto toNotificationDto(NxvNotifications n) {
        return NotificationDto.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getUser() != null ? n.getUser().getUserId() : null)
                .isGlobal(n.getUser() == null)
                .title(n.getTitle())
                .content(n.getContent())
                .notificationType(n.getNotificationType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private UserReportDto toUserReportDto(NxvUserReports r) {
        String stLabel = switch (r.getStatus() != null ? r.getStatus() : 0) {
            case 1 -> "RESOLVED (Đã giải quyết)";
            case 2 -> "REJECTED (Đã từ chối)";
            default -> "PENDING (Đang chờ xử lý)";
        };
        return UserReportDto.builder()
                .reportId(r.getReportId())
                .reporterUserId(r.getReporterUser().getUserId())
                .reporterUsername(r.getReporterUser().getUsername())
                .targetUserId(r.getTargetUser() != null ? r.getTargetUser().getUserId() : null)
                .targetUsername(r.getTargetUser() != null ? r.getTargetUser().getUsername() : null)
                .reportType(r.getReportType())
                .description(r.getDescription())
                .status(r.getStatus())
                .statusLabel(stLabel)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
"@

# ==============================================================================
# 4. SINH REST CONTROLLER CHO MODULE 8 (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvSystemAdminController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvInventoryDtos.ItemDetailDto;
import $basePkg.nxvdto.NxvSystemAdminDtos.*;
import $basePkg.nxventity.NxvAIConfig;
import $basePkg.nxventity.NxvGachaBanners;
import $basePkg.nxventity.NxvTags;
import $basePkg.nxvservice.NxvSystemAdminService;
import $basePkg.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin-system")
@RequiredArgsConstructor
@Tag(name = "Module 8: Quản trị viên Admin Dashboard & Hệ thống (Chức năng #103 - #115)", description = "Thống kê tổng quan, Ban/Unban Users, Phân quyền, Cấu hình động Gacha/Item/Banner/Phạt HP, Cronjobs, Global Notifications, Log Vàng, Report & AI Config")
public class NxvSystemAdminController {

    private final NxvSystemAdminService systemAdminService;

    @Operation(summary = "[#103] Dashboard thống kê tổng quan toàn hệ thống (Users, Tasks, Gacha, Marketplace, Doanh thu Hồi sinh VNĐ)")
    @GetMapping("/dashboard/summary")
    public ApiResponse<AdminDashboardSummaryDto> getDashboardSummary() {
        return ApiResponse.ok(systemAdminService.getSystemDashboardSummary());
    }

    @Operation(summary = "[#104A] Xem danh sách toàn bộ tài khoản người dùng trong bảng Users")
    @GetMapping("/users")
    public ApiResponse<List<AdminUserRowDto>> getAllUsers() {
        return ApiResponse.ok(systemAdminService.getAllUsersForAdmin());
    }

    @Operation(summary = "[#104B] Khóa / Mở khóa tài khoản người dùng (Ban / Unban bảng Users)")
    @PostMapping("/users/{targetUserId}/ban-toggle")
    public ApiResponse<AdminUserRowDto> toggleBanUser(@PathVariable Long targetUserId,
                                                      @RequestParam(defaultValue = "true") boolean lock) {
        return ApiResponse.ok(lock ? "Đã khóa (Ban) tài khoản thành công!" : "Đã mở khóa (Unban) tài khoản thành công!",
                systemAdminService.toggleBanUser(targetUserId, lock));
    }

    @Operation(summary = "[#113] Phân quyền Admin đa cấp (0 = User, 1 = Moderator, 2 = SuperAdmin)")
    @PutMapping("/users/{targetUserId}/role")
    public ApiResponse<AdminUserRowDto> changeUserRole(@PathVariable Long targetUserId,
                                                       @Valid @RequestBody ChangeUserRoleRequest request) {
        return ApiResponse.ok("Cập nhật phân quyền tài khoản thành công!", systemAdminService.changeUserRole(targetUserId, request));
    }

    // --- ITEM, BANNER & GACHA DYNAMIC CONFIG (#105, #106, #107) ---
    @Operation(summary = "[#106A] Thêm mới Vật phẩm vào danh mục (Gắn icon 2D tĩnh, setup chỉ số Buff)")
    @PostMapping("/items")
    public ApiResponse<ItemDetailDto> createItem(@Valid @RequestBody UpsertItemRequest request) {
        return ApiResponse.ok("Thêm mới vật phẩm vào hệ thống thành công!", systemAdminService.createCatalogItem(request));
    }

    @Operation(summary = "[#106B] Chỉnh sửa thông số Buff / Giá bán / Ảnh 2D tĩnh của Vật phẩm")
    @PutMapping("/items/{itemId}")
    public ApiResponse<ItemDetailDto> updateItem(@PathVariable Integer itemId,
                                                 @RequestBody UpsertItemRequest request) {
        return ApiResponse.ok("Cập nhật vật phẩm thành công!", systemAdminService.updateCatalogItem(itemId, request));
    }

    @Operation(summary = "[#107A] Tạo mới Banner Hộp mù (Gắn ảnh tĩnh, Giá mở, Mốc bảo hiểm Pity)")
    @PostMapping("/gacha/banners")
    public ApiResponse<NxvGachaBanners> createBanner(@Valid @RequestBody UpsertGachaBannerRequest request) {
        return ApiResponse.ok("Tạo mới Banner Hộp mù thành công!", systemAdminService.createGachaBanner(request));
    }

    @Operation(summary = "[#107B, #105] Cập nhật Banner Hộp mù (Đổi giá quay, chỉnh sửa mốc Pity, đóng/mở Banner)")
    @PutMapping("/gacha/banners/{bannerId}")
    public ApiResponse<NxvGachaBanners> updateBanner(@PathVariable Integer bannerId,
                                                     @RequestBody UpsertGachaBannerRequest request) {
        return ApiResponse.ok("Cập nhật Banner Hộp mù thành công!", systemAdminService.updateGachaBanner(bannerId, request));
    }

    @Operation(summary = "[#105] Cấu hình động Tỷ lệ rớt đồ (Drop Rate) và Vật phẩm Featured cho Banner Gacha")
    @PostMapping("/gacha/drop-rates")
    public ApiResponse<Map<String, Object>> upsertDropRate(@Valid @RequestBody UpsertDropRateRequest request) {
        return ApiResponse.ok("Cập nhật tỷ lệ rớt đồ Gacha thành công!", systemAdminService.upsertGachaDropRate(request));
    }

    // --- DIFFICULTY PENALTY & CRONJOB MONITOR (#108, #109) ---
    @Operation(summary = "[#108A] Xem cấu hình hệ số Phạt HP và Thưởng mặc định cho từng độ khó của Task")
    @GetMapping("/config/difficulty-penalties")
    public ApiResponse<DifficultyPenaltyConfigDto> getDifficultyConfig() {
        return ApiResponse.ok(systemAdminService.getDifficultyPenaltyConfig());
    }

    @Operation(summary = "[#108B] Cập nhật hệ số Phạt (Lượng HP bị trừ) mặc định cho từng độ khó của Task")
    @PutMapping("/config/difficulty-penalties")
    public ApiResponse<Map<String, Object>> updateDifficultyConfig(
            @RequestBody DifficultyPenaltyConfigDto request,
            @RequestParam(defaultValue = "false") boolean applyToExistingPendingTasks) {
        return ApiResponse.ok("Đã cập nhật hệ số phạt độ khó!", systemAdminService.updateDifficultyPenaltyConfig(request, applyToExistingPendingTasks));
    }

    @Operation(summary = "[#109] Theo dõi và kích hoạt chạy kiểm tra toàn bộ các Cronjob ngầm (Trừ máu Task quá hạn & Pet ngủ đông)")
    @PostMapping("/cronjobs/run-all")
    public ApiResponse<Map<String, Object>> runAllCronjobs() {
        return ApiResponse.ok("Đã thực thi toàn bộ Cronjob hệ thống!", systemAdminService.triggerAllSystemCronjobsManually());
    }

    // --- GLOBAL NOTIFICATIONS (#110) ---
    @Operation(summary = "[#110A] Gửi thông báo (Global Notification đẩy đến toàn bộ Client nếu targetUserId = null)")
    @PostMapping("/notifications/send")
    public ApiResponse<NotificationDto> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        return ApiResponse.ok("Đã phát thông báo thành công!", systemAdminService.sendNotification(request));
    }

    @Operation(summary = "[#110B] Xem danh sách thông báo của tôi và Global Notifications")
    @GetMapping("/notifications/me")
    public ApiResponse<List<NotificationDto>> getMyNotifications() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(systemAdminService.getMyAndGlobalNotifications(userId));
    }

    @Operation(summary = "[#110C] Đánh dấu đã đọc thông báo")
    @PatchMapping("/notifications/{notificationId}/read")
    public ApiResponse<NotificationDto> markRead(@PathVariable Long notificationId) {
        return ApiResponse.ok(systemAdminService.markNotificationRead(notificationId));
    }

    // --- SYSTEM LOGS, TAGS, REPORTS & AI CONFIG (#111, #112, #114, #115) ---
    @Operation(summary = "[#111] Xem Log hệ thống (Lịch sử nạp/trừ Vàng, EXP, HP toàn Server để phát hiện bug)")
    @GetMapping("/logs/stat-logs")
    public ApiResponse<List<Map<String, Object>>> getGlobalStatLogs(@RequestParam(required = false) String logType) {
        return ApiResponse.ok(systemAdminService.getGlobalServerStatLogs(logType));
    }

    @Operation(summary = "[#121-ADMIN] Đối soát toàn bộ Hóa đơn tiền thật (Billing) & Lịch sử Webhook Ngân hàng")
    @GetMapping("/logs/billing-webhooks")
    public ApiResponse<Map<String, Object>> getBillingAndWebhookLogs() {
        return ApiResponse.ok(systemAdminService.getAllBillingAndWebhookLogsForAdmin());
    }

    @Operation(summary = "[#112A] Cập nhật Thẻ chuyên môn (Tag) mặc định của hệ thống")
    @PutMapping("/tags/{tagId}")
    public ApiResponse<NxvTags> updateTag(@PathVariable Integer tagId,
                                          @RequestParam String tagName,
                                          @RequestParam(required = false) String colorHex) {
        return ApiResponse.ok("Cập nhật Tag thành công!", systemAdminService.updateSystemTag(tagId, tagName, colorHex));
    }

    @Operation(summary = "[#112B] Xóa Thẻ chuyên môn (Tag)")
    @DeleteMapping("/tags/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Integer tagId) {
        systemAdminService.deleteSystemTag(tagId);
        return ApiResponse.ok("Đã xóa Tag thành công!", null);
    }

    @Operation(summary = "[#114A] Người chơi gửi Khiếu nại / Báo cáo lỗi (Report)")
    @PostMapping("/reports")
    public ApiResponse<UserReportDto> submitReport(@Valid @RequestBody CreateUserReportRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã gửi báo cáo lên Ban quản trị!", systemAdminService.submitUserReport(userId, request));
    }

    @Operation(summary = "[#114B] Admin xem danh sách Khiếu nại / Report từ người dùng")
    @GetMapping("/reports")
    public ApiResponse<List<UserReportDto>> getAllReports(@RequestParam(required = false) Integer status) {
        return ApiResponse.ok(systemAdminService.getAllUserReports(status));
    }

    @Operation(summary = "[#114C] Admin duyệt và xử lý Khiếu nại / Report (1 = Resolved, 2 = Rejected)")
    @PutMapping("/reports/{reportId}/resolve")
    public ApiResponse<UserReportDto> resolveReport(@PathVariable Long reportId,
                                                    @Valid @RequestBody ResolveReportRequest request) {
        return ApiResponse.ok("Đã xử lý báo cáo khiếu nại!", systemAdminService.resolveUserReport(reportId, request));
    }

    @Operation(summary = "[#115A] Xem cấu hình API kết nối AI Chatbot hiện tại")
    @GetMapping("/ai-config")
    public ApiResponse<List<NxvAIConfig>> getAiConfigs() {
        return ApiResponse.ok(systemAdminService.getAllAiConfigs());
    }

    @Operation(summary = "[#115B] Cập nhật cấu hình API kết nối AI Chatbot (Đổi Token / Model / System Prompt)")
    @PutMapping("/ai-config")
    public ApiResponse<NxvAIConfig> upsertAiConfig(@Valid @RequestBody UpsertAiConfigRequest request) {
        return ApiResponse.ok("Cập nhật cấu hình AI Chatbot thành công!", systemAdminService.upsertAiConfig(request));
    }
}
"@

Write-Host "==> HOAN TAT SINH MODULE 8 (TASK 9: ADMIN DASHBOARD & TOAN BO 122/122 CHUC NANG BACKEND) THANH CONG!" -ForegroundColor Green