# ==============================================================================
# TASKTALE / LIFERPG - TASK 2 AUTOMATED GENERATOR (MODULE 2: CHARACTER & RPG STATS)
# Hoan thien Chuc nang #12 -> #21 & #101: Dashboard, Avatar/Frame, Titles,
# Achievements, StatLogs, Level Progression Calculator, Daily Check-in & Profile Card
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

Write-Host "==> Dang sinh Module 2 (Character, RPG Stats, Titles, Achievements, Profile Card) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 2
# ==============================================================================
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
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserTitlesRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserTitles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserTitlesRepository extends JpaRepository<NxvUserTitles, Long> {
    List<NxvUserTitles> findByUser_UserIdOrderByUnlockedAtDesc(Long userId);
    boolean existsByUser_UserIdAndTitle_TitleId(Long userId, Integer titleId);
    Optional<NxvUserTitles> findByUser_UserIdAndTitle_TitleId(Long userId, Integer titleId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvAchievementsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvAchievements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NxvAchievementsRepository extends JpaRepository<NxvAchievements, Integer> {
    Optional<NxvAchievements> findByCode(String code);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserAchievementsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserAchievements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvUserAchievementsRepository extends JpaRepository<NxvUserAchievements, Long> {
    List<NxvUserAchievements> findByUser_UserIdOrderByUnlockedAtDesc(Long userId);
    boolean existsByUser_UserIdAndAchievement_AchievementId(Long userId, Integer achievementId);
    boolean existsByUser_UserIdAndAchievement_Code(Long userId, String code);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvEquippedItemsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvEquippedItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvEquippedItemsRepository extends JpaRepository<NxvEquippedItems, Long> {
    List<NxvEquippedItems> findByUser_UserId(Long userId);
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 2 (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvCharacterDtos.java" @"
package $basePkg.nxvdto;

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
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 2 (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvCharacterService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvCharacterDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvenums.NxvConstants;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
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
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_OWNED, "Bạn chưa sở hữu vật phẩm Avatar này trong túi đồ!"));
            if (inv.getItem().getItemType() != NxvConstants.ITEM_AVATAR) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Vật phẩm này không phải là Ảnh đại diện (Avatar 2D tĩnh)!");
            }
            c.setAvatarUrl(inv.getItem().getImageUrl());
        } else if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) {
            c.setAvatarUrl(req.getAvatarUrl().trim());
        } else {
            throw new AppException(ErrorCode.BAD_REQUEST, "Vui lòng truyền itemId hoặc avatarUrl hợp lệ!");
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
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_OWNED, "Bạn chưa sở hữu Khung Avatar này trong túi đồ!"));
            if (inv.getItem().getItemType() != NxvConstants.ITEM_AVATAR_FRAME) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Vật phẩm này không phải là Khung Avatar!");
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
                throw new AppException(ErrorCode.FORBIDDEN, "Bạn chưa mở khóa Danh hiệu này!");
            }
            NxvTitles title = titlesRepository.findById(req.getTitleId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Danh hiệu không tồn tại!"));
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
            throw new AppException(ErrorCode.BAD_REQUEST, "Hôm nay bạn đã điểm danh rồi! Hãy quay lại vào ngày mai nhé.");
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
                .titleName(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleName() : "Tân Binh")
                .titleIcon(c.getCurrentTitle() != null ? c.getCurrentTitle().getTitleIcon() : null)
                .unlockedAchievementsCount(userAchs.size())
                .equippedItemNames(equippedNames)
                .badgeNames(badges)
                .build();
    }

    private NxvCharacters getCharacterByUserId(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Nhân vật của người dùng ID: " + userId));
    }
}
"@

# ==============================================================================
# 4. SINH REST CONTROLLER CHO MODULE 2 (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvCharacterController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvCharacterDtos.*;
import $basePkg.nxvservice.NxvCharacterService;
import $basePkg.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
@Tag(name = "Module 2: Nhân vật, Chỉ số RPG & Thành tựu (Chức năng #12 - #21, #101)", description = "Dashboard HP/MP/EXP/Gold, Đổi Avatar/Khung tĩnh, Danh hiệu, Lịch sử chỉ số & Profile Card")
public class NxvCharacterController {

    private final NxvCharacterService characterService;

    @Operation(summary = "[#13] Xem Dashboard trạng thái Nhân vật hiện tại (HP, MP, EXP, Gold, Base/Effective Stats & Tổng Buff)")
    @GetMapping("/me/dashboard")
    public ApiResponse<CharacterDashboardDto> getMyDashboard() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getDashboard(userId));
    }

    @Operation(summary = "[#12] Đổi tên Nhân vật")
    @PutMapping("/me/name")
    public ApiResponse<CharacterDashboardDto> renameCharacter(@Valid @RequestBody UpdateCharacterNameRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đổi tên nhân vật thành công!", characterService.renameCharacter(userId, request));
    }

    @Operation(summary = "[#14] Đổi Avatar 2D tĩnh của Nhân vật (Từ vật phẩm Gacha hoặc URL tĩnh)")
    @PutMapping("/me/avatar")
    public ApiResponse<CharacterDashboardDto> changeAvatar(@RequestBody ChangeAvatarRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật Avatar 2D tĩnh thành công!", characterService.changeAvatar(userId, request));
    }

    @Operation(summary = "[#18] Thay đổi Khung Avatar tĩnh (Avatar Frame)")
    @PutMapping("/me/avatar-frame")
    public ApiResponse<CharacterDashboardDto> changeAvatarFrame(@RequestBody ChangeAvatarFrameRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật Khung Avatar thành công!", characterService.changeAvatarFrame(userId, request));
    }

    @Operation(summary = "[#19A] Xem danh sách toàn bộ Danh hiệu (Titles/Badges) và trạng thái mở khóa")
    @GetMapping("/me/titles")
    public ApiResponse<List<TitleDto>> getMyTitles() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getAllTitlesForUser(userId));
    }

    @Operation(summary = "[#19B] Thay đổi Danh hiệu đang hiển thị (Equip/Unequip Title)")
    @PutMapping("/me/title")
    public ApiResponse<CharacterDashboardDto> equipTitle(@RequestBody EquipTitleRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật danh hiệu hiển thị thành công!", characterService.equipTitle(userId, request));
    }

    @Operation(summary = "[#15, #20] Xem bảng tính toán EXP lũy tiến và HP/MP gốc theo từng cấp độ")
    @GetMapping("/level-progression")
    public ApiResponse<List<LevelStepDto>> getLevelProgression(@RequestParam(defaultValue = "20") int maxLevel) {
        return ApiResponse.ok(characterService.getLevelProgressionTable(maxLevel));
    }

    @Operation(summary = "[#15, #16] Điểm danh hàng ngày (Daily Check-in) tăng Streak, nhận Gold/EXP và tự kiểm tra Level Up")
    @PostMapping("/me/check-in")
    public ApiResponse<Map<String, Object>> dailyCheckIn() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Điểm danh hàng ngày thành công!", characterService.dailyCheckIn(userId));
    }

    @Operation(summary = "[#17] Xem lịch sử biến động chỉ số (Stat Logs: EXP, Gold, HP, MP, LevelUp)")
    @GetMapping("/me/stat-logs")
    public ApiResponse<List<StatLogDto>> getMyStatLogs(@RequestParam(required = false) String logType) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getStatLogs(userId, logType));
    }

    @Operation(summary = "[#101] Xem danh sách Thành tựu (Achievements) và tự động nhận thưởng khi đủ điều kiện")
    @GetMapping("/me/achievements")
    public ApiResponse<List<AchievementDto>> getMyAchievements() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getMyAchievements(userId));
    }

    @Operation(summary = "[#21, #101] Xem Thẻ thông tin tĩnh (Profile Card) của người chơi bất kỳ để chia sẻ MXH")
    @GetMapping("/profile-card/{targetUserId}")
    public ApiResponse<PublicProfileCardDto> getPublicProfileCard(@PathVariable Long targetUserId) {
        return ApiResponse.ok(characterService.getPublicProfileCard(targetUserId));
    }
}
"@

Write-Host "==> HOAN TAT SINH MODULE 2 (TASK 2: CHARACTER, RPG STATS, TITLES, ACHIEVEMENTS, PROFILE CARD) THANH CONG!" -ForegroundColor Green