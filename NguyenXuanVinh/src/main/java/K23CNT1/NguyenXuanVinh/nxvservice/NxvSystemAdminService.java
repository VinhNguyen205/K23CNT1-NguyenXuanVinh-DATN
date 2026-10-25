package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvInventoryDtos.ItemDetailDto;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvSystemAdminDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
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
                case 2 -> "SUPER_ADMIN (Quáº£n trá»‹ tá»‘i cao)";
                case 1 -> "MODERATOR (Äiá»u hÃ nh viÃªn)";
                default -> "USER (NgÆ°á»i chÆ¡i)";
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng ID: " + targetUserId));
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
            throw new AppException(ErrorCode.BAD_REQUEST, "Role chá»‰ nháº­n giÃ¡ trá»‹: 0 (User), 1 (Moderator), 2 (SuperAdmin)!");
        }
        Users u = usersRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng ID: " + targetUserId));
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
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Slot trang bá»‹ ID: " + req.getEquipSlotId()));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y váº­t pháº©m ID: " + itemId));

        if (req.getItemName() != null && !req.getItemName().isBlank()) item.setItemName(req.getItemName().trim());
        if (req.getItemType() != null) item.setItemType(req.getItemType());
        if (req.getImageUrl() != null) item.setImageUrl(req.getImageUrl().trim());
        if (req.getEquipSlotId() != null) {
            NxvEquipmentSlots slot = equipmentSlotsRepository.findById(req.getEquipSlotId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Slot trang bá»‹ ID: " + req.getEquipSlotId()));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Banner ID: " + bannerId));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Banner ID: " + req.getBannerId()));
        NxvItems item = itemsRepository.findById(req.getItemId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Váº­t pháº©m ID: " + req.getItemId()));

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
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i nháº­n ID: " + req.getTargetUserId()));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y thÃ´ng bÃ¡o ID: " + notificationId));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Tag ID: " + tagId));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i gá»­i bÃ¡o cÃ¡o!"));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y bÃ¡o cÃ¡o/khiáº¿u náº¡i ID: " + reportId));
        r.setStatus(req.getStatus());
        r = userReportsRepository.save(r);

        notificationsRepository.save(NxvNotifications.builder()
                .user(r.getReporterUser())
                .title("BÃ¡o cÃ¡o #" + reportId + " Ä‘Ã£ Ä‘Æ°á»£c Admin xá»­ lÃ½")
                .content("Khiáº¿u náº¡i [" + r.getReportType() + "] cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c cáº­p nháº­t tráº¡ng thÃ¡i: "
                        + (req.getStatus() == 1 ? "ÄÃƒ GIáº¢I QUYáº¾T" : "Tá»ª CHá»I")
                        + (req.getAdminNote() != null ? ". Ghi chÃº Admin: " + req.getAdminNote() : ""))
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
            case 1 -> "RESOLVED (ÄÃ£ giáº£i quyáº¿t)";
            case 2 -> "REJECTED (ÄÃ£ tá»« chá»‘i)";
            default -> "PENDING (Äang chá» xá»­ lÃ½)";
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