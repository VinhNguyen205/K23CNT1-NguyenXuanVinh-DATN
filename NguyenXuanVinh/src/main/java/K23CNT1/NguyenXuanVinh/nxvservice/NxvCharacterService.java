package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvCharacterDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvCharacterService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvLevelConfigRepository levelConfigRepository;
    private final NxvVwUserTotalBuffsRepository totalBuffsRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvTitlesRepository titlesRepository;
    private final NxvUserTitlesRepository userTitlesRepository;
    private final NxvAchievementsRepository achievementsRepository;
    private final NxvUserAchievementsRepository userAchievementsRepository;
    private final NxvInventoryRepository inventoryRepository;
    private final NxvEquippedItemsRepository equippedItemsRepository;

    // Chuc nang #13: Dashboard trang thai Nhan vat (HP, MP, EXP, Gold, Buffs)
    @Transactional(readOnly = true)
    public CharacterDashboardDto getDashboard(Long userId) {
        NxvCharacters c = getCharacterByUserId(userId);
        Users u = c.getUser();
        NxvVwUserTotalBuffs buffs = totalBuffsRepository.findById(userId).orElse(null);

        int buffHp = (buffs != null && buffs.getTotalBuffMaxHp() != null) ? buffs.getTotalBuffMaxHp() : 0;
        int buffMp = (buffs != null && buffs.getTotalBuffMaxMp() != null) ? buffs.getTotalBuffMaxMp() : 0;
        BigDecimal buffExp = (buffs != null && buffs.getTotalExpBonus() != null) ? buffs.getTotalExpBonus() : BigDecimal.ZERO;
        BigDecimal buffGold = (buffs != null && buffs.getTotalGoldBonus() != null) ? buffs.getTotalGoldBonus() : BigDecimal.ZERO;
        BigDecimal buffDmgRed = (buffs != null && buffs.getTotalDmgReduce() != null) ? buffs.getTotalDmgReduce() : BigDecimal.ZERO;

        double pct = 0.0;
        if (c.getExpToNextLevel() != null && c.getExpToNextLevel() > 0) {
            pct = BigDecimal.valueOf(c.getCurrentExp() * 100.0 / c.getExpToNextLevel())
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return CharacterDashboardDto.builder()
                .characterId(c.getCharacterId())
                .userId(u.getUserId())
                .username(u.getUsername())
                .characterName(c.getCharacterName())
                .gender(u.getGender())
                .isDead(u.getIsDead())
                .avatarUrl(c.getAvatarUrl())
                .avatarFrameUrl(c.getAvatarFrameUrl())
                .currentLevel(c.getCurrentLevel())
                .currentExp(c.getCurrentExp())
                .expToNextLevel(c.getExpToNextLevel())
                .expProgressPercent(pct)
                .baseMaxHp(c.getBaseMaxHp())
                .maxHp(c.getMaxHp())
                .currentHp(c.getCurrentHp())
                .baseMaxMp(c.getBaseMaxMp())
                .maxMp(c.getMaxMp())
                .currentMp(c.getCurrentMp())
                .gold(c.getGold())
                .skillPoints(c.getSkillPoints())
                .isShieldActive(c.getIsShieldActive())
                .totalTasksDone(c.getTotalTasksDone())
                .streakDays(c.getStreakDays())
                .lastCheckInDate(c.getLastCheckInDate())
                .currentClassId(c.getCurrentClass() != null ? c.getCurrentClass().getClassId() : null)
                .currentClassName(c.getCurrentClass() != null ? c.getCurrentClass().getClassName() : "Novice Adventurer")
                .currentTitleId(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleId() : null)
                .currentTitleName(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleName() : null)
                .currentTitleIcon(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleIcon() : null)
                .totalBuffMaxHp(buffHp)
                .totalBuffMaxMp(buffMp)
                .totalExpBonus(buffExp)
                .totalGoldBonus(buffGold)
                .totalDmgReduce(buffDmgRed)
                .build();
    }

    // Chuc nang #12: Doi ten Nhan vat
    @Transactional
    public CharacterDashboardDto renameCharacter(Long userId, UpdateCharacterNameRequest req) {
        NxvCharacters c = getCharacterByUserId(userId);
        c.setCharacterName(req.getCharacterName().trim());
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);
        return getDashboard(userId);
    }

    // Chuc nang #14: Doi Avatar 2D tinh (Tu vat pham Gacha ItemType=2 hoac URL tinh)
    @Transactional
    public CharacterDashboardDto changeAvatar(Long userId, ChangeAvatarRequest req) {
        NxvCharacters c = getCharacterByUserId(userId);
        if (req.getItemId() != null) {
            NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(userId, req.getItemId())
                    .filter(i -> i.getQuantity() > 0)
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_OWNED, "Báº¡n chÆ°a sá»Ÿ há»¯u váº­t pháº©m Avatar nÃ y trong tÃºi Ä‘á»“!"));
            if (inv.getItem().getItemType() != NxvConstants.ITEM_AVATAR) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Váº­t pháº©m nÃ y khÃ´ng pháº£i lÃ  áº¢nh Ä‘áº¡i diá»‡n (Avatar 2D tÄ©nh)!");
            }
            c.setAvatarUrl(inv.getItem().getImageUrl());
        } else if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) {
            c.setAvatarUrl(req.getAvatarUrl().trim());
        } else {
            throw new AppException(ErrorCode.BAD_REQUEST, "Vui lÃ²ng truyá»n itemId hoáº·c avatarUrl há»£p lá»‡!");
        }
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);
        return getDashboard(userId);
    }

    // Chuc nang #18: Thay doi Khung Avatar tinh (Avatar Frame - ItemType=3 hoac URL)
    @Transactional
    public CharacterDashboardDto changeAvatarFrame(Long userId, ChangeAvatarFrameRequest req) {
        NxvCharacters c = getCharacterByUserId(userId);
        if (req.getItemId() != null) {
            NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(userId, req.getItemId())
                    .filter(i -> i.getQuantity() > 0)
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_OWNED, "Báº¡n chÆ°a sá»Ÿ há»¯u Khung Avatar nÃ y trong tÃºi Ä‘á»“!"));
            if (inv.getItem().getItemType() != NxvConstants.ITEM_AVATAR_FRAME) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Váº­t pháº©m nÃ y khÃ´ng pháº£i lÃ  Khung Avatar!");
            }
            c.setAvatarFrameUrl(inv.getItem().getImageUrl());
        } else {
            c.setAvatarFrameUrl(req.getAvatarFrameUrl() != null && !req.getAvatarFrameUrl().isBlank()
                    ? req.getAvatarFrameUrl().trim() : null);
        }
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);
        return getDashboard(userId);
    }

    // Chuc nang #19: Danh sach Danh hieu & Thay doi Danh hieu hien thi (Title/Badge)
    @Transactional(readOnly = true)
    public List<TitleDto> getAllTitlesForUser(Long userId) {
        NxvCharacters c = getCharacterByUserId(userId);
        Integer equippedTitleId = (c.getCurrentTitle() != null) ? c.getCurrentTitle().getTitleId() : null;

        Map<Integer, LocalDateTime> unlockedMap = userTitlesRepository.findByUser_UserIdOrderByUnlockedAtDesc(userId)
                .stream()
                .collect(Collectors.toMap(
                        ut -> ut.getTitle().getTitleId(),
                        NxvUserTitles::getUnlockedAt,
                        (a, b) -> a
                ));

        return titlesRepository.findAll().stream()
                .map(t -> TitleDto.builder()
                        .titleId(t.getTitleId())
                        .titleName(t.getTitleName())
                        .titleIcon(t.getTitleIcon())
                        .isExclusive(t.getIsExclusive())
                        .isUnlocked(unlockedMap.containsKey(t.getTitleId()))
                        .isEquipped(Objects.equals(t.getTitleId(), equippedTitleId))
                        .unlockedAt(unlockedMap.get(t.getTitleId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CharacterDashboardDto equipTitle(Long userId, EquipTitleRequest req) {
        NxvCharacters c = getCharacterByUserId(userId);
        if (req.getTitleId() == null) {
            c.setCurrentTitle(null);
        } else {
            if (!userTitlesRepository.existsByUser_UserIdAndTitle_TitleId(userId, req.getTitleId())) {
                throw new AppException(ErrorCode.FORBIDDEN, "Báº¡n chÆ°a má»Ÿ khÃ³a Danh hiá»‡u nÃ y!");
            }
            NxvTitles title = titlesRepository.findById(req.getTitleId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Danh hiá»‡u khÃ´ng tá»“n táº¡i!"));
            c.setCurrentTitle(title);
        }
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);
        return getDashboard(userId);
    }

    // Chuc nang #15 & #20: Thuat toan tinh toan EXP luy tien & Bang tra cuu Level
    @Transactional(readOnly = true)
    public List<LevelStepDto> getLevelProgressionTable(int maxLevel) {
        int limit = Math.min(Math.max(maxLevel, 10), 100);
        Map<Integer, NxvLevelConfig> dbConfigs = levelConfigRepository.findAll().stream()
                .collect(Collectors.toMap(NxvLevelConfig::getLevel, cfg -> cfg));

        List<LevelStepDto> result = new ArrayList<>();
        long prevExp = 100L;
        int prevHp = 100;
        int prevMp = 50;

        for (int lv = 1; lv <= limit; lv++) {
            NxvLevelConfig cfg = dbConfigs.get(lv);
            if (cfg != null) {
                prevExp = cfg.getExpRequired();
                prevHp = cfg.getMaxHp();
                prevMp = cfg.getMaxMp();
                result.add(LevelStepDto.builder()
                        .level(lv)
                        .expRequired(prevExp)
                        .baseMaxHp(prevHp)
                        .baseMaxMp(prevMp)
                        .isConfiguredInDb(true)
                        .build());
            } else {
                // Cong thuc luy tien khop 100% voi Stored Procedure NxvSp_CompleteTask (v2.2)
                prevExp = (long) Math.floor(prevExp * 1.25);
                prevHp += 20;
                prevMp += 10;
                result.add(LevelStepDto.builder()
                        .level(lv)
                        .expRequired(prevExp)
                        .baseMaxHp(prevHp)
                        .baseMaxMp(prevMp)
                        .isConfiguredInDb(false)
                        .build());
            }
        }
        return result;
    }

    // Chuc nang #15: Ham cong EXP & Tu dong Level Up (Dong bo BaseMaxHP/BaseMaxMP + Buff v2.2)
    @Transactional
    public int addExpAndCheckLevelUp(NxvCharacters c, long expDelta, String sourceType, Long sourceId) {
        if (expDelta <= 0) return 0;
        Long userId = c.getUser().getUserId();

        c.setCurrentExp(c.getCurrentExp() + expDelta);
        statLogsRepository.save(NxvStatLogs.builder()
                .user(c.getUser())
                .logType("EXP")
                .delta(expDelta)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .createdAt(LocalDateTime.now())
                .build());

        NxvVwUserTotalBuffs buffs = totalBuffsRepository.findById(userId).orElse(null);
        int equipAddHp = (buffs != null && buffs.getTotalBuffMaxHp() != null) ? buffs.getTotalBuffMaxHp() : 0;
        int equipAddMp = (buffs != null && buffs.getTotalBuffMaxMp() != null) ? buffs.getTotalBuffMaxMp() : 0;

        int levelsGained = 0;
        while (c.getCurrentExp() >= c.getExpToNextLevel()) {
            c.setCurrentExp(c.getCurrentExp() - c.getExpToNextLevel());
            int newLevel = c.getCurrentLevel() + 1;
            c.setCurrentLevel(newLevel);
            levelsGained++;

            NxvLevelConfig cfg = levelConfigRepository.findById(newLevel).orElse(null);
            long newNextExp;
            int newBaseHp;
            int newBaseMp;

            if (cfg != null) {
                newNextExp = cfg.getExpRequired();
                newBaseHp = cfg.getMaxHp();
                newBaseMp = cfg.getMaxMp();
            } else {
                newNextExp = (long) Math.floor(c.getExpToNextLevel() * 1.25);
                newBaseHp = c.getBaseMaxHp() + 20;
                newBaseMp = c.getBaseMaxMp() + 10;
            }

            c.setExpToNextLevel(newNextExp);
            c.setBaseMaxHp(newBaseHp);
            c.setBaseMaxMp(newBaseMp);
            c.setMaxHp(newBaseHp + equipAddHp);
            c.setCurrentHp(newBaseHp + equipAddHp);
            c.setMaxMp(newBaseMp + equipAddMp);
            c.setCurrentMp(newBaseMp + equipAddMp);
            c.setSkillPoints(c.getSkillPoints() + 1);
        }

        if (levelsGained > 0) {
            statLogsRepository.save(NxvStatLogs.builder()
                    .user(c.getUser())
                    .logType("LevelUp")
                    .delta((long) levelsGained)
                    .sourceType(sourceType)
                    .sourceId(sourceId)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);
        return levelsGained;
    }

    // Diem danh hang ngay (Daily Check-in): Tang StreakDays, thuong EXP/Gold & Mo khoa Thanh tuu Streak
    @Transactional
    public Map<String, Object> dailyCheckIn(Long userId) {
        NxvCharacters c = getCharacterByUserId(userId);
        if (Boolean.TRUE.equals(c.getUser().getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }

        LocalDate today = LocalDate.now();
        if (today.equals(c.getLastCheckInDate())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "HÃ´m nay báº¡n Ä‘Ã£ Ä‘iá»ƒm danh rá»“i! HÃ£y quay láº¡i vÃ o ngÃ y mai nhÃ©.");
        }

        if (c.getLastCheckInDate() != null && c.getLastCheckInDate().plusDays(1).equals(today)) {
            c.setStreakDays(c.getStreakDays() + 1);
        } else {
            c.setStreakDays(1);
        }
        c.setLastCheckInDate(today);

        long rewardGold = 20L + Math.min(c.getStreakDays() * 5L, 50L);
        long rewardExp = 30L + Math.min(c.getStreakDays() * 5L, 70L);

        c.setGold(c.getGold() + rewardGold);
        statLogsRepository.save(NxvStatLogs.builder()
                .user(c.getUser())
                .logType("Gold")
                .delta(rewardGold)
                .sourceType("DailyCheckIn")
                .createdAt(LocalDateTime.now())
                .build());

        int levelsGained = addExpAndCheckLevelUp(c, rewardExp, "DailyCheckIn", null);
        List<String> unlockedAchievements = evaluateAndUnlockAchievements(userId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("streakDays", c.getStreakDays());
        res.put("goldGained", rewardGold);
        res.put("expGained", rewardExp);
        res.put("levelsGained", levelsGained);
        res.put("newlyUnlockedAchievements", unlockedAchievements);
        res.put("dashboard", getDashboard(userId));
        return res;
    }

    // Chuc nang #17: Xem lich su bien dong chi so (Transaction Log: EXP, Gold, HP, MP, LevelUp)
    @Transactional(readOnly = true)
    public List<StatLogDto> getStatLogs(Long userId, String logType) {
        List<NxvStatLogs> logs = (logType != null && !logType.isBlank())
                ? statLogsRepository.findTop100ByUser_UserIdAndLogTypeIgnoreCaseOrderByCreatedAtDesc(userId, logType.trim())
                : statLogsRepository.findTop100ByUser_UserIdOrderByCreatedAtDesc(userId);

        return logs.stream()
                .map(l -> StatLogDto.builder()
                        .logId(l.getLogId())
                        .logType(l.getLogType())
                        .delta(l.getDelta())
                        .sourceType(l.getSourceType())
                        .sourceId(l.getSourceId())
                        .createdAt(l.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Chuc nang #101: He thong Thanh tuu (Achievements) - Xem danh sach & Tu dong kiem tra trao thuong
    @Transactional
    public List<AchievementDto> getMyAchievements(Long userId) {
        evaluateAndUnlockAchievements(userId);
        Map<Integer, LocalDateTime> unlockedMap = userAchievementsRepository.findByUser_UserIdOrderByUnlockedAtDesc(userId)
                .stream()
                .collect(Collectors.toMap(
                        ua -> ua.getAchievement().getAchievementId(),
                        NxvUserAchievements::getUnlockedAt,
                        (a, b) -> a
                ));

        return achievementsRepository.findAll().stream()
                .map(a -> AchievementDto.builder()
                        .achievementId(a.getAchievementId())
                        .code(a.getCode())
                        .achievementName(a.getAchievementName())
                        .description(a.getDescription())
                        .iconUrl(a.getIconUrl())
                        .rewardGold(a.getRewardGold())
                        .rewardExp(a.getRewardExp())
                        .rewardTitleId(a.getRewardTitle() != null ? a.getRewardTitle().getTitleId() : null)
                        .rewardTitleName(a.getRewardTitle() != null ? a.getRewardTitle().getTitleName() : null)
                        .rewardItemId(a.getRewardItem() != null ? a.getRewardItem().getItemId() : null)
                        .isUnlocked(unlockedMap.containsKey(a.getAchievementId()))
                        .unlockedAt(unlockedMap.get(a.getAchievementId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<String> evaluateAndUnlockAchievements(Long userId) {
        NxvCharacters c = getCharacterByUserId(userId);
        List<String> newlyUnlocked = new ArrayList<>();

        for (NxvAchievements ach : achievementsRepository.findAll()) {
            if (userAchievementsRepository.existsByUser_UserIdAndAchievement_AchievementId(userId, ach.getAchievementId())) {
                continue;
            }
            boolean matched = false;
            if ("FIRST_TASK_DONE".equalsIgnoreCase(ach.getCode()) && c.getTotalTasksDone() >= 1) {
                matched = true;
            } else if ("STREAK_7_DAYS".equalsIgnoreCase(ach.getCode()) && c.getStreakDays() >= 7) {
                matched = true;
            }

            if (matched) {
                grantAchievementToUser(c, ach);
                newlyUnlocked.add(ach.getAchievementName());
            }
        }
        return newlyUnlocked;
    }

    private void grantAchievementToUser(NxvCharacters c, NxvAchievements ach) {
        Users u = c.getUser();
        userAchievementsRepository.save(NxvUserAchievements.builder()
                .user(u)
                .achievement(ach)
                .unlockedAt(LocalDateTime.now())
                .build());

        if (ach.getRewardTitle() != null &&
                !userTitlesRepository.existsByUser_UserIdAndTitle_TitleId(u.getUserId(), ach.getRewardTitle().getTitleId())) {
            userTitlesRepository.save(NxvUserTitles.builder()
                    .user(u)
                    .title(ach.getRewardTitle())
                    .unlockedAt(LocalDateTime.now())
                    .build());
        }

        if (ach.getRewardItem() != null) {
            NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(u.getUserId(), ach.getRewardItem().getItemId())
                    .orElseGet(() -> NxvInventory.builder()
                            .user(u)
                            .item(ach.getRewardItem())
                            .quantity(0)
                            .build());
            inv.setQuantity(inv.getQuantity() + 1);
            inventoryRepository.save(inv);
        }

        if (ach.getRewardGold() != null && ach.getRewardGold() > 0) {
            c.setGold(c.getGold() + ach.getRewardGold());
            statLogsRepository.save(NxvStatLogs.builder()
                    .user(u)
                    .logType("Gold")
                    .delta(ach.getRewardGold().longValue())
                    .sourceType("Achievement")
                    .sourceId(ach.getAchievementId().longValue())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (ach.getRewardExp() != null && ach.getRewardExp() > 0) {
            addExpAndCheckLevelUp(c, ach.getRewardExp().longValue(), "Achievement", ach.getAchievementId().longValue());
        } else {
            charactersRepository.save(c);
        }
    }

    // Chuc nang #21 & #101: Xem Profile Card tinh cua nguoi choi khac (Share to Social)
    @Transactional(readOnly = true)
    public PublicProfileCardDto getPublicProfileCard(Long targetUserId) {
        NxvCharacters c = getCharacterByUserId(targetUserId);
        Users u = c.getUser();

        List<String> equippedNames = equippedItemsRepository.findByUser_UserId(targetUserId).stream()
                .map(ei -> ei.getItem().getItemName())
                .collect(Collectors.toList());

        List<NxvUserAchievements> userAchs = userAchievementsRepository.findByUser_UserIdOrderByUnlockedAtDesc(targetUserId);
        List<String> badges = userAchs.stream()
                .map(ua -> ua.getAchievement().getAchievementName())
                .collect(Collectors.toList());

        return PublicProfileCardDto.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .characterName(c.getCharacterName())
                .avatarUrl(c.getAvatarUrl())
                .avatarFrameUrl(c.getAvatarFrameUrl())
                .currentLevel(c.getCurrentLevel())
                .gold(c.getGold())
                .totalTasksDone(c.getTotalTasksDone())
                .streakDays(c.getStreakDays())
                .className(c.getCurrentClass() != null ? c.getCurrentClass().getClassName() : "Novice Adventurer")
                .titleName(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleName() : "TÃ¢n Binh")
                .titleIcon(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleIcon() : null)
                .unlockedAchievementsCount(userAchs.size())
                .equippedItemNames(equippedNames)
                .badgeNames(badges)
                .build();
    }

    private NxvCharacters getCharacterByUserId(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y NhÃ¢n váº­t cá»§a ngÆ°á»i dÃ¹ng ID: " + userId));
    }
}