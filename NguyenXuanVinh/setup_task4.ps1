# ==============================================================================
# TASKTALE / LIFERPG - TASK 4 AUTOMATED GENERATOR (MODULE 4: SKILL TREE & CLASS)
# Hoan thien Chuc nang #42 -> #56: Tag Mastery, Dynamic Class Recommendation,
# Change Class, Skill Tree, Unlock/Upgrade/Reset Skills, Active Skills (Dash 24h & Shield)
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

Write-Host "==> Dang sinh Module 4 (Skill Tree, Dynamic Class, Active Skills Dash/Shield) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 4
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvUserUnlockedClassesRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserUnlockedClasses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvUserUnlockedClassesRepository extends JpaRepository<NxvUserUnlockedClasses, Long> {
    List<NxvUserUnlockedClasses> findByUser_UserIdOrderByUnlockedAtDesc(Long userId);
    boolean existsByUser_UserIdAndNxvClass_ClassId(Long userId, Integer classId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvClassChangeLogsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvClassChangeLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvClassChangeLogsRepository extends JpaRepository<NxvClassChangeLogs, Long> {
    List<NxvClassChangeLogs> findTop50ByUser_UserIdOrderByChangedAtDesc(Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserSkillsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserSkills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserSkillsRepository extends JpaRepository<NxvUserSkills, Long> {
    List<NxvUserSkills> findByUser_UserId(Long userId);
    Optional<NxvUserSkills> findByUser_UserIdAndSkill_SkillId(Long userId, Integer skillId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvSkillUsageLogsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvSkillUsageLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvSkillUsageLogsRepository extends JpaRepository<NxvSkillUsageLogs, Long> {
    List<NxvSkillUsageLogs> findTop100ByUser_UserIdOrderByUsedAtDesc(Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserTagMasteryRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserTagMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserTagMasteryRepository extends JpaRepository<NxvUserTagMastery, Long> {
    List<NxvUserTagMastery> findByUser_UserIdOrderByTaskCountDesc(Long userId);
    Optional<NxvUserTagMastery> findByUser_UserIdAndTag_TagId(Long userId, Integer tagId);
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 4 (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvSkillClassDtos.java" @"
package $basePkg.nxvdto;

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
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 4 (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvSkillClassService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvSkillClassDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvSkillClassService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvClassesRepository classesRepository;
    private final NxvUserUnlockedClassesRepository unlockedClassesRepository;
    private final NxvClassChangeLogsRepository classChangeLogsRepository;
    private final NxvSkillDefinitionsRepository skillDefinitionsRepository;
    private final NxvUserSkillsRepository userSkillsRepository;
    private final NxvSkillUsageLogsRepository skillUsageLogsRepository;
    private final NxvUserTagMasteryRepository userTagMasteryRepository;
    private final NxvTagsRepository tagsRepository;
    private final NxvTasksRepository tasksRepository;
    private final NxvTaskHistoryRepository taskHistoryRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;

    public static final long CHANGE_CLASS_COST_GOLD = 200L;
    public static final long RESET_SKILL_TREE_COST_GOLD = 150L;

    // =========================================================================
    // PHAN 1: HE THONG CLASS DONG & DIEM CHUYEN MON THEO TAG (#42, #43, #52, #53)
    // =========================================================================

    @Transactional(readOnly = true)
    public ClassOverviewDto getClassAndMasteryOverview(Long userId) {
        NxvCharacters c = getCharacter(userId);
        List<NxvUserTagMastery> masteries = userTagMasteryRepository.findByUser_UserIdOrderByTaskCountDesc(userId);

        List<TagMasteryDto> masteryDtos = masteries.stream()
                .map(m -> TagMasteryDto.builder()
                        .tagId(m.getTag().getTagId())
                        .tagName(m.getTag().getTagName())
                        .colorHex(m.getTag().getColorHex())
                        .taskCount(m.getTaskCount())
                        .updatedAt(m.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        List<NxvClasses> allClasses = classesRepository.findAll();
        Set<Integer> unlockedIds = unlockedClassesRepository.findByUser_UserIdOrderByUnlockedAtDesc(userId).stream()
                .map(uc -> uc.getNxvClass().getClassId())
                .collect(Collectors.toSet());

        // Thuat toan #43: Phan tich Tag Mastery de de xuat Class phu hop
        int techScore = 0;
        int physicalScore = 0;
        for (NxvUserTagMastery m : masteries) {
            String tName = m.getTag().getTagName().toLowerCase();
            if (tName.contains("code") || tName.contains("backend") || tName.contains("frontend")
                    || tName.contains("java") || tName.contains("react") || tName.contains("sql")
                    || tName.contains("học") || tName.contains("it")) {
                techScore += m.getTaskCount();
            } else if (tName.contains("thể thao") || tName.contains("gym") || tName.contains("calisthenics")
                    || tName.contains("chạy") || tName.contains("vận động") || tName.contains("sức khỏe")) {
                physicalScore += m.getTaskCount();
            }
        }

        NxvClasses recommendedClass = null;
        String reason;
        if (!allClasses.isEmpty()) {
            if (physicalScore > techScore && allClasses.size() >= 2) {
                recommendedClass = allClasses.stream()
                        .filter(cl -> cl.getClassName().toLowerCase().contains("knight") || cl.getClassId() == 2)
                        .findFirst().orElse(allClasses.get(0));
                reason = "Bạn có điểm chuyên môn nhóm Thể chất/Vận động cao (" + physicalScore + " tasks) -> Phù hợp với " + recommendedClass.getClassName();
            } else {
                recommendedClass = allClasses.stream()
                        .filter(cl -> cl.getClassName().toLowerCase().contains("mage") || cl.getClassId() == 1)
                        .findFirst().orElse(allClasses.get(0));
                reason = "Dựa trên tần suất làm việc nhóm Công nghệ/Học tập (" + techScore + " tasks) -> Đề xuất chuyển chức thành " + recommendedClass.getClassName();
            }
        } else {
            reason = "Chưa có dữ liệu Class trong hệ thống.";
        }

        Integer currentClassId = c.getCurrentClass() != null ? c.getCurrentClass().getClassId() : null;
        Integer recClassId = recommendedClass != null ? recommendedClass.getClassId() : null;
        long changeCost = (currentClassId == null) ? 0L : CHANGE_CLASS_COST_GOLD;

        List<ClassInfoDto> classDtos = allClasses.stream()
                .map(cl -> ClassInfoDto.builder()
                        .classId(cl.getClassId())
                        .className(cl.getClassName())
                        .passiveExpBonus(cl.getPassiveExpBonus())
                        .passiveGoldBonus(cl.getPassiveGoldBonus())
                        .isUnlocked(unlockedIds.contains(cl.getClassId()))
                        .isCurrentClass(Objects.equals(cl.getClassId(), currentClassId))
                        .isRecommended(Objects.equals(cl.getClassId(), recClassId))
                        .build())
                .collect(Collectors.toList());

        return ClassOverviewDto.builder()
                .userId(userId)
                .currentClassId(currentClassId)
                .currentClassName(c.getCurrentClass() != null ? c.getCurrentClass().getClassName() : "Novice Adventurer (Chưa chuyển chức)")
                .currentPassiveExpBonus(c.getCurrentClass() != null ? c.getCurrentClass().getPassiveExpBonus() : BigDecimal.ZERO)
                .currentPassiveGoldBonus(c.getCurrentClass() != null ? c.getCurrentClass().getPassiveGoldBonus() : BigDecimal.ZERO)
                .recommendedClassId(recClassId)
                .recommendedClassName(recommendedClass != null ? recommendedClass.getClassName() : null)
                .recommendationReason(reason)
                .changeClassCostGold(changeCost)
                .currentGold(c.getGold())
                .tagMasteries(masteryDtos)
                .classes(classDtos)
                .build();
    }

    // Chuc nang #53: Doi Class nhan vat (Lan dau mien phi, cac lan sau ton Vang)
    @Transactional
    public ClassOverviewDto changeCharacterClass(Long userId, ChangeClassRequest req) {
        assertUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvClasses targetClass = classesRepository.findById(req.getClassId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Class ID: " + req.getClassId()));

        NxvClasses oldClass = c.getCurrentClass();
        if (oldClass != null && Objects.equals(oldClass.getClassId(), targetClass.getClassId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Nhân vật của bạn đang ở Class [" + targetClass.getClassName() + "] rồi!");
        }

        // Neu da co Class truoc do thi ton CHANGE_CLASS_COST_GOLD (200 Gold)
        if (oldClass != null) {
            if (c.getGold() < CHANGE_CLASS_COST_GOLD) {
                throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                        "Không đủ Vàng! Cần " + CHANGE_CLASS_COST_GOLD + " Gold để chuyển đổi Class.");
            }
            c.setGold(c.getGold() - CHANGE_CLASS_COST_GOLD);
            statLogsRepository.save(NxvStatLogs.builder()
                    .user(c.getUser())
                    .logType("Gold")
                    .delta(-CHANGE_CLASS_COST_GOLD)
                    .sourceType("ChangeClass")
                    .sourceId(targetClass.getClassId().longValue())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (!unlockedClassesRepository.existsByUser_UserIdAndNxvClass_ClassId(userId, targetClass.getClassId())) {
            unlockedClassesRepository.save(NxvUserUnlockedClasses.builder()
                    .user(c.getUser())
                    .nxvClass(targetClass)
                    .unlockedAt(LocalDateTime.now())
                    .build());
        }

        c.setCurrentClass(targetClass);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        classChangeLogsRepository.save(NxvClassChangeLogs.builder()
                .user(c.getUser())
                .fromClass(oldClass)
                .toClass(targetClass)
                .changedAt(LocalDateTime.now())
                .build());

        return getClassAndMasteryOverview(userId);
    }

    @Transactional(readOnly = true)
    public List<ClassChangeLogDto> getClassChangeLogs(Long userId) {
        return classChangeLogsRepository.findTop50ByUser_UserIdOrderByChangedAtDesc(userId).stream()
                .map(l -> ClassChangeLogDto.builder()
                        .logId(l.getLogId())
                        .fromClassId(l.getFromClass() != null ? l.getFromClass().getClassId() : null)
                        .fromClassName(l.getFromClass() != null ? l.getFromClass().getClassName() : "Chưa có Class")
                        .toClassId(l.getToClass().getClassId())
                        .toClassName(l.getToClass().getClassName())
                        .changedAt(l.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Helper dung chung khi hoan thanh Task co gan Tag (#42)
    @Transactional
    public void recordTagMasteryProgress(Users user, List<NxvTags> tags) {
        if (tags == null || tags.isEmpty()) return;
        for (NxvTags tag : tags) {
            NxvUserTagMastery mastery = userTagMasteryRepository
                    .findByUser_UserIdAndTag_TagId(user.getUserId(), tag.getTagId())
                    .orElseGet(() -> NxvUserTagMastery.builder()
                            .user(user)
                            .tag(tag)
                            .taskCount(0)
                            .build());
            mastery.setTaskCount(mastery.getTaskCount() + 1);
            mastery.setUpdatedAt(LocalDateTime.now());
            userTagMasteryRepository.save(mastery);
        }
    }

    // =========================================================================
    // PHAN 2: CAY KY NANG (SKILL TREE) & ACTIVE SKILLS (#44 -> #51, #54 -> #56)
    // =========================================================================

    // Chuc nang #45 & #49: Xem Cay ky nang dang nhanh tinh & dem nguoc Cooldown
    @Transactional(readOnly = true)
    public SkillTreeOverviewDto getSkillTreeOverview(Long userId) {
        NxvCharacters c = getCharacter(userId);
        Map<Integer, NxvUserSkills> userSkillMap = userSkillsRepository.findByUser_UserId(userId).stream()
                .collect(Collectors.toMap(us -> us.getSkill().getSkillId(), us -> us));

        LocalDateTime now = LocalDateTime.now();
        int totalSpent = userSkillMap.values().stream()
                .filter(us -> Boolean.TRUE.equals(us.getIsUnlocked()))
                .mapToInt(NxvUserSkills::getSkillLevel)
                .sum();

        List<SkillNodeDto> nodes = skillDefinitionsRepository.findAll().stream()
                .map(def -> {
                    NxvUserSkills us = userSkillMap.get(def.getSkillId());
                    boolean unlocked = (us != null && Boolean.TRUE.equals(us.getIsUnlocked()));
                    int curLv = unlocked ? us.getSkillLevel() : 0;
                    LocalDateTime cdEnd = (us != null) ? us.getCooldownEndsAt() : null;
                    long remainingSec = (cdEnd != null && cdEnd.isAfter(now))
                            ? Duration.between(now, cdEnd).getSeconds() : 0L;

                    boolean parentUnlocked = (def.getParentSkill() == null) ||
                            (userSkillMap.containsKey(def.getParentSkill().getSkillId())
                                    && Boolean.TRUE.equals(userSkillMap.get(def.getParentSkill().getSkillId()).getIsUnlocked()));

                    boolean canUnlock = !unlocked
                            && c.getCurrentLevel() >= def.getRequiredLevel()
                            && parentUnlocked
                            && c.getSkillPoints() >= 1;

                    boolean canUpgrade = unlocked
                            && curLv < def.getMaxSkillLevel()
                            && c.getSkillPoints() >= 1;

                    int effectiveCd = calculateEffectiveCooldown(def.getCooldownSeconds(), Math.max(curLv, 1));

                    return SkillNodeDto.builder()
                            .skillId(def.getSkillId())
                            .skillName(def.getSkillName())
                            .skillType(def.getSkillType())
                            .skillTypeLabel(def.getSkillType() == 0 ? "Kỹ năng Chủ động (Active)" : "Kỹ năng Thụ động (Passive)")
                            .mpCost(def.getMpCost())
                            .baseCooldownSeconds(def.getCooldownSeconds())
                            .effectiveCooldownSeconds(effectiveCd)
                            .requiredLevel(def.getRequiredLevel())
                            .maxSkillLevel(def.getMaxSkillLevel())
                            .parentSkillId(def.getParentSkill() != null ? def.getParentSkill().getSkillId() : null)
                            .parentSkillName(def.getParentSkill() != null ? def.getParentSkill().getSkillName() : null)
                            .skillEffect(def.getSkillEffect())
                            .isUnlocked(unlocked)
                            .currentSkillLevel(curLv)
                            .cooldownEndsAt(cdEnd)
                            .remainingCooldownSeconds(remainingSec)
                            .canUnlock(canUnlock)
                            .canUpgrade(canUpgrade)
                            .build();
                })
                .collect(Collectors.toList());

        return SkillTreeOverviewDto.builder()
                .userId(userId)
                .characterLevel(c.getCurrentLevel())
                .availableSkillPoints(c.getSkillPoints())
                .totalPointsSpent(totalSpent)
                .currentMp(c.getCurrentMp())
                .maxMp(c.getMaxMp())
                .currentGold(c.getGold())
                .resetTreeCostGold(RESET_SKILL_TREE_COST_GOLD)
                .isShieldActive(c.getIsShieldActive())
                .skills(nodes)
                .build();
    }

    // Chuc nang #46: Mo khoa ky nang moi khi dat cap do yeu cau (Ton 1 SkillPoint)
    @Transactional
    public SkillTreeOverviewDto unlockSkill(Long userId, Integer skillId) {
        assertUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvSkillDefinitions def = skillDefinitionsRepository.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Kỹ năng ID: " + skillId));

        if (c.getCurrentLevel() < def.getRequiredLevel()) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Nhân vật cần đạt Cấp độ " + def.getRequiredLevel() + " để mở khóa kỹ năng [" + def.getSkillName() + "]!");
        }

        if (def.getParentSkill() != null) {
            boolean parentUnlocked = userSkillsRepository
                    .findByUser_UserIdAndSkill_SkillId(userId, def.getParentSkill().getSkillId())
                    .map(NxvUserSkills::getIsUnlocked)
                    .orElse(false);
            if (!parentUnlocked) {
                throw new AppException(ErrorCode.BAD_REQUEST,
                        "Cần mở khóa kỹ năng nhánh cha [" + def.getParentSkill().getSkillName() + "] trước!");
            }
        }

        NxvUserSkills userSkill = userSkillsRepository.findByUser_UserIdAndSkill_SkillId(userId, skillId)
                .orElseGet(() -> NxvUserSkills.builder()
                        .user(c.getUser())
                        .skill(def)
                        .isUnlocked(false)
                        .skillLevel(1)
                        .build());

        if (Boolean.TRUE.equals(userSkill.getIsUnlocked())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Kỹ năng này đã được mở khóa rồi!");
        }
        if (c.getSkillPoints() < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không đủ Điểm kỹ năng (Skill Points)! Hãy lên cấp để nhận thêm điểm.");
        }

        c.setSkillPoints(c.getSkillPoints() - 1);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        userSkill.setIsUnlocked(true);
        userSkill.setSkillLevel(1);
        userSkillsRepository.save(userSkill);

        return getSkillTreeOverview(userId);
    }

    // Chuc nang #47: Nang cap (Upgrade) cap do cua tung Ky nang (Ton 1 SkillPoint)
    @Transactional
    public SkillTreeOverviewDto upgradeSkill(Long userId, Integer skillId) {
        assertUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserSkills userSkill = userSkillsRepository.findByUser_UserIdAndSkill_SkillId(userId, skillId)
                .filter(us -> Boolean.TRUE.equals(us.getIsUnlocked()))
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Bạn phải mở khóa kỹ năng này trước khi nâng cấp!"));

        NxvSkillDefinitions def = userSkill.getSkill();
        if (userSkill.getSkillLevel() >= def.getMaxSkillLevel()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Kỹ năng [" + def.getSkillName() + "] đã đạt cấp độ tối đa (" + def.getMaxSkillLevel() + ")!");
        }
        if (c.getSkillPoints() < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không đủ Điểm kỹ năng (Skill Points) để nâng cấp!");
        }

        c.setSkillPoints(c.getSkillPoints() - 1);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        userSkill.setSkillLevel(userSkill.getSkillLevel() + 1);
        userSkillsRepository.save(userSkill);

        return getSkillTreeOverview(userId);
    }

    // Chuc nang #48, #49, #50, #51, #55, #56: Kich hoat Ky nang chu dong (Active Skill: Dash 24h & Shield)
    @Transactional
    public Map<String, Object> castActiveSkill(Long userId, Integer skillId, CastSkillRequest req) {
        assertUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserSkills userSkill = userSkillsRepository.findByUser_UserIdAndSkill_SkillId(userId, skillId)
                .filter(us -> Boolean.TRUE.equals(us.getIsUnlocked()))
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Bạn chưa mở khóa kỹ năng này!"));

        NxvSkillDefinitions def = userSkill.getSkill();
        if (def.getSkillType() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Đây là Kỹ năng Thụ động (Passive), không thể kích hoạt thủ công!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (userSkill.getCooldownEndsAt() != null && userSkill.getCooldownEndsAt().isAfter(now)) {
            long rem = Duration.between(now, userSkill.getCooldownEndsAt()).getSeconds();
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Kỹ năng [" + def.getSkillName() + "] đang hồi chiêu! Còn lại " + rem + " giây.");
        }

        if (c.getCurrentMp() < def.getMpCost()) {
            throw new AppException(ErrorCode.INSUFFICIENT_MP,
                    "Không đủ Mana (MP)! Cần " + def.getMpCost() + " MP (Hiện có: " + c.getCurrentMp() + " MP).");
        }

        Long targetTaskId = (req != null) ? req.getTargetTaskId() : null;
        NxvTasks targetTask = null;
        String effectDescription;

        String effectCode = (def.getSkillEffect() != null) ? def.getSkillEffect().trim().toUpperCase() : "";
        if ("DASH_24H".equals(effectCode)) {
            // Chuc nang #51: Chieu Luot (Dash) - Gia han Deadline them 24h cho 1 Task
            if (targetTaskId == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Chiêu Lướt (DASH_24H) bắt buộc phải truyền targetTaskId cần gia hạn!");
            }
            targetTask = tasksRepository.findById(targetTaskId)
                    .filter(t -> Objects.equals(t.getUser().getUserId(), userId))
                    .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, "Không tìm thấy Task của bạn với ID: " + targetTaskId));

            if (targetTask.getStatus() != 0) {
                throw new AppException(ErrorCode.TASK_ALREADY_PROCESSED, "Chỉ có thể gia hạn cho Task đang ở trạng thái Chờ thực hiện (Pending)!");
            }

            LocalDateTime baseDeadline = (targetTask.getDeadlineAt() != null && targetTask.getDeadlineAt().isAfter(now))
                    ? targetTask.getDeadlineAt() : now;
            LocalDateTime newDeadline = baseDeadline.plusHours(24);
            targetTask.setDeadlineAt(newDeadline);
            targetTask.setUpdatedAt(now);
            tasksRepository.save(targetTask);

            taskHistoryRepository.save(NxvTaskHistory.builder()
                    .task(targetTask)
                    .user(c.getUser())
                    .actionType("SKILL_DASH_24H")
                    .actionAt(now)
                    .build());

            effectDescription = "Đã kích hoạt [" + def.getSkillName() + "]! Task \"" + targetTask.getTitle() + "\" được gia hạn thêm 24 giờ (Deadline mới: " + newDeadline + ").";
        } else if ("SHIELD_TASK".equals(effectCode)) {
            // Chuc nang #50: Khien Chan (Shield) - Bao ve Task & Nhan vat khong bi tru mau khi tre han
            c.setIsShieldActive(true);
            if (targetTaskId != null) {
                targetTask = tasksRepository.findById(targetTaskId)
                        .filter(t -> Objects.equals(t.getUser().getUserId(), userId))
                        .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, "Không tìm thấy Task của bạn với ID: " + targetTaskId));
                targetTask.setIsShielded(true);
                targetTask.setUpdatedAt(now);
                tasksRepository.save(targetTask);

                taskHistoryRepository.save(NxvTaskHistory.builder()
                        .task(targetTask)
                        .user(c.getUser())
                        .actionType("SKILL_SHIELD")
                        .actionAt(now)
                        .build());

                effectDescription = "Đã bật [" + def.getSkillName() + "] bảo vệ Task \"" + targetTask.getTitle() + "\" và Nhân vật khỏi bị trừ máu khi trễ hạn!";
            } else {
                effectDescription = "Đã bật [" + def.getSkillName() + "] trên người Nhân vật! Tự động chặn sát thương cho lần trễ hạn tiếp theo.";
            }
        } else {
            effectDescription = "Đã kích hoạt kỹ năng [" + def.getSkillName() + "] thành công!";
        }

        // Tru MP & Tinh Cooldown (#48, #49)
        c.setCurrentMp(c.getCurrentMp() - def.getMpCost());
        c.setUpdatedAt(now);
        charactersRepository.save(c);

        int effectiveCooldown = calculateEffectiveCooldown(def.getCooldownSeconds(), userSkill.getSkillLevel());
        LocalDateTime cdEnd = now.plusSeconds(effectiveCooldown);
        userSkill.setCooldownEndsAt(cdEnd);
        userSkillsRepository.save(userSkill);

        // Ghi Log MP, Lich su Skill (#55) & Thong bao hieu ung (#56)
        if (def.getMpCost() > 0) {
            statLogsRepository.save(NxvStatLogs.builder()
                    .user(c.getUser())
                    .logType("MP")
                    .delta((long) -def.getMpCost())
                    .sourceType("SkillCast")
                    .sourceId(def.getSkillId().longValue())
                    .createdAt(now)
                    .build());
        }

        skillUsageLogsRepository.save(NxvSkillUsageLogs.builder()
                .user(c.getUser())
                .skill(def)
                .targetTask(targetTask)
                .usedAt(now)
                .build());

        notificationsRepository.save(NxvNotifications.builder()
                .user(c.getUser())
                .title("Kích hoạt kỹ năng: " + def.getSkillName())
                .content(effectDescription)
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(now)
                .build());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("skillId", def.getSkillId());
        res.put("skillName", def.getSkillName());
        res.put("skillLevel", userSkill.getSkillLevel());
        res.put("mpSpent", def.getMpCost());
        res.put("remainingMp", c.getCurrentMp());
        res.put("cooldownEndsAt", cdEnd);
        res.put("effectMessage", effectDescription);
        return res;
    }

    // Chuc nang #54: Reset toan bo Cay ky nang de cong lai (Ton Vang)
    @Transactional
    public SkillTreeOverviewDto resetSkillTree(Long userId) {
        assertUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        List<NxvUserSkills> unlockedSkills = userSkillsRepository.findByUser_UserId(userId).stream()
                .filter(us -> Boolean.TRUE.equals(us.getIsUnlocked()))
                .collect(Collectors.toList());

        if (unlockedSkills.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bạn chưa cộng điểm vào kỹ năng nào để Reset!");
        }
        if (c.getGold() < RESET_SKILL_TREE_COST_GOLD) {
            throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                    "Không đủ Vàng! Cần " + RESET_SKILL_TREE_COST_GOLD + " Gold để Reset toàn bộ Cây kỹ năng.");
        }

        int refundedPoints = 0;
        for (NxvUserSkills us : unlockedSkills) {
            refundedPoints += us.getSkillLevel();
            us.setIsUnlocked(false);
            us.setSkillLevel(1);
            userSkillsRepository.save(us);
        }

        c.setGold(c.getGold() - RESET_SKILL_TREE_COST_GOLD);
        c.setSkillPoints(c.getSkillPoints() + refundedPoints);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(c.getUser())
                .logType("Gold")
                .delta(-RESET_SKILL_TREE_COST_GOLD)
                .sourceType("ResetSkillTree")
                .createdAt(LocalDateTime.now())
                .build());

        return getSkillTreeOverview(userId);
    }

    // Chuc nang #55: Xem lich su su dung ky nang
    @Transactional(readOnly = true)
    public List<SkillUsageLogDto> getSkillUsageLogs(Long userId) {
        return skillUsageLogsRepository.findTop100ByUser_UserIdOrderByUsedAtDesc(userId).stream()
                .map(l -> SkillUsageLogDto.builder()
                        .usageId(l.getUsageId())
                        .skillId(l.getSkill().getSkillId())
                        .skillName(l.getSkill().getSkillName())
                        .skillEffect(l.getSkill().getSkillEffect())
                        .targetTaskId(l.getTargetTask() != null ? l.getTargetTask().getTaskId() : null)
                        .targetTaskTitle(l.getTargetTask() != null ? l.getTargetTask().getTitle() : null)
                        .usedAt(l.getUsedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Moi cap do ky nang tang them giam 10% thoi gian hoi chieu (toi da giam 40% o cap 5)
    private int calculateEffectiveCooldown(Integer baseCooldown, int skillLevel) {
        if (baseCooldown == null || baseCooldown <= 0) return 0;
        double multiplier = Math.max(0.5, 1.0 - (Math.max(skillLevel, 1) - 1) * 0.10);
        return (int) Math.round(baseCooldown * multiplier);
    }

    private void assertUserAlive(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));
    }
}
"@

# ==============================================================================
# 4. SINH REST CONTROLLER CHO MODULE 4 (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvSkillClassController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvSkillClassDtos.*;
import $basePkg.nxvservice.NxvSkillClassService;
import $basePkg.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/skills-classes")
@RequiredArgsConstructor
@Tag(name = "Module 4: Cây Kỹ Năng & Lớp Nhân Vật (Chức năng #42 - #56)", description = "Theo dõi Tag Mastery, Đề xuất & Đổi Class động, Mở khóa/Nâng cấp/Reset Cây kỹ năng, Dùng chiêu Khiên Chắn & Lướt 24h")
public class NxvSkillClassController {

    private final NxvSkillClassService skillClassService;

    @Operation(summary = "[#42, #43, #52] Xem điểm chuyên môn theo Tag, đề xuất chuyển Class động và chỉ số Buff thụ động từ Class")
    @GetMapping("/classes/overview")
    public ApiResponse<ClassOverviewDto> getClassOverview() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getClassAndMasteryOverview(userId));
    }

    @Operation(summary = "[#53] Chọn hoặc Đổi Class nhân vật (Tech Mage, Physical Knight...)")
    @PostMapping("/classes/change")
    public ApiResponse<ClassOverviewDto> changeClass(@Valid @RequestBody ChangeClassRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Chuyển đổi Class nhân vật thành công!", skillClassService.changeCharacterClass(userId, request));
    }

    @Operation(summary = "[#53B] Xem lịch sử chuyển đổi Class của nhân vật")
    @GetMapping("/classes/history")
    public ApiResponse<List<ClassChangeLogDto>> getClassHistory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getClassChangeLogs(userId));
    }

    @Operation(summary = "[#44, #45, #49] Xem giao diện Cây kỹ năng (Skill Tree) dạng nhánh tĩnh, SkillPoints và thời gian Cooldown")
    @GetMapping("/tree")
    public ApiResponse<SkillTreeOverviewDto> getSkillTree() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getSkillTreeOverview(userId));
    }

    @Operation(summary = "[#46] Mở khóa kỹ năng mới bằng Skill Point (Kiểm tra cấp độ yêu cầu & kỹ năng nhánh cha)")
    @PostMapping("/skills/{skillId}/unlock")
    public ApiResponse<SkillTreeOverviewDto> unlockSkill(@PathVariable Integer skillId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mở khóa kỹ năng mới thành công!", skillClassService.unlockSkill(userId, skillId));
    }

    @Operation(summary = "[#47] Nâng cấp (Upgrade) cấp độ kỹ năng bằng Skill Point (Giảm thời gian hồi chiêu)")
    @PostMapping("/skills/{skillId}/upgrade")
    public ApiResponse<SkillTreeOverviewDto> upgradeSkill(@PathVariable Integer skillId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Nâng cấp kỹ năng thành công!", skillClassService.upgradeSkill(userId, skillId));
    }

    @Operation(summary = "[#48, #50, #51, #56] Kích hoạt Kỹ năng chủ động (Trừ MP): Chiêu Lướt (DASH_24H gia hạn 24h) & Khiên Chắn (SHIELD_TASK)")
    @PostMapping("/skills/{skillId}/cast")
    public ApiResponse<Map<String, Object>> castSkill(@PathVariable Integer skillId,
                                                      @RequestBody(required = false) CastSkillRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Kích hoạt kỹ năng thành công!", skillClassService.castActiveSkill(userId, skillId, request));
    }

    @Operation(summary = "[#54] Reset toàn bộ Cây kỹ năng để hoàn lại toàn bộ Skill Points (Tốn Vàng)")
    @PostMapping("/tree/reset")
    public ApiResponse<SkillTreeOverviewDto> resetSkillTree() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã Reset Cây kỹ năng và hoàn trả toàn bộ điểm kỹ năng!", skillClassService.resetSkillTree(userId));
    }

    @Operation(summary = "[#55] Xem lịch sử sử dụng kỹ năng (Skill Usage Logs)")
    @GetMapping("/skills/usage-logs")
    public ApiResponse<List<SkillUsageLogDto>> getSkillUsageLogs() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getSkillUsageLogs(userId));
    }
}
"@

Write-Host "==> HOAN TAT SINH MODULE 4 (TASK 4: SKILL TREE, DYNAMIC CLASS, ACTIVE SKILLS DASH/SHIELD) THANH CONG!" -ForegroundColor Green