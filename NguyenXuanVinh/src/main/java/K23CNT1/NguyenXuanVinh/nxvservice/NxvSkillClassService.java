package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvSkillClassDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
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
                    || tName.contains("há»c") || tName.contains("it")) {
                techScore += m.getTaskCount();
            } else if (tName.contains("thá»ƒ thao") || tName.contains("gym") || tName.contains("calisthenics")
                    || tName.contains("cháº¡y") || tName.contains("váº­n Ä‘á»™ng") || tName.contains("sá»©c khá»e")) {
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
                reason = "Báº¡n cÃ³ Ä‘iá»ƒm chuyÃªn mÃ´n nhÃ³m Thá»ƒ cháº¥t/Váº­n Ä‘á»™ng cao (" + physicalScore + " tasks) -> PhÃ¹ há»£p vá»›i " + recommendedClass.getClassName();
            } else {
                recommendedClass = allClasses.stream()
                        .filter(cl -> cl.getClassName().toLowerCase().contains("mage") || cl.getClassId() == 1)
                        .findFirst().orElse(allClasses.get(0));
                reason = "Dá»±a trÃªn táº§n suáº¥t lÃ m viá»‡c nhÃ³m CÃ´ng nghá»‡/Há»c táº­p (" + techScore + " tasks) -> Äá» xuáº¥t chuyá»ƒn chá»©c thÃ nh " + recommendedClass.getClassName();
            }
        } else {
            reason = "ChÆ°a cÃ³ dá»¯ liá»‡u Class trong há»‡ thá»‘ng.";
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
                .currentClassName(c.getCurrentClass() != null ? c.getCurrentClass().getClassName() : "Novice Adventurer (ChÆ°a chuyá»ƒn chá»©c)")
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Class ID: " + req.getClassId()));

        NxvClasses oldClass = c.getCurrentClass();
        if (oldClass != null && Objects.equals(oldClass.getClassId(), targetClass.getClassId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "NhÃ¢n váº­t cá»§a báº¡n Ä‘ang á»Ÿ Class [" + targetClass.getClassName() + "] rá»“i!");
        }

        // Neu da co Class truoc do thi ton CHANGE_CLASS_COST_GOLD (200 Gold)
        if (oldClass != null) {
            if (c.getGold() < CHANGE_CLASS_COST_GOLD) {
                throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                        "KhÃ´ng Ä‘á»§ VÃ ng! Cáº§n " + CHANGE_CLASS_COST_GOLD + " Gold Ä‘á»ƒ chuyá»ƒn Ä‘á»•i Class.");
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
                        .fromClassName(l.getFromClass() != null ? l.getFromClass().getClassName() : "ChÆ°a cÃ³ Class")
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
                            .skillTypeLabel(def.getSkillType() == 0 ? "Ká»¹ nÄƒng Chá»§ Ä‘á»™ng (Active)" : "Ká»¹ nÄƒng Thá»¥ Ä‘á»™ng (Passive)")
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ká»¹ nÄƒng ID: " + skillId));

        if (c.getCurrentLevel() < def.getRequiredLevel()) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "NhÃ¢n váº­t cáº§n Ä‘áº¡t Cáº¥p Ä‘á»™ " + def.getRequiredLevel() + " Ä‘á»ƒ má»Ÿ khÃ³a ká»¹ nÄƒng [" + def.getSkillName() + "]!");
        }

        if (def.getParentSkill() != null) {
            boolean parentUnlocked = userSkillsRepository
                    .findByUser_UserIdAndSkill_SkillId(userId, def.getParentSkill().getSkillId())
                    .map(NxvUserSkills::getIsUnlocked)
                    .orElse(false);
            if (!parentUnlocked) {
                throw new AppException(ErrorCode.BAD_REQUEST,
                        "Cáº§n má»Ÿ khÃ³a ká»¹ nÄƒng nhÃ¡nh cha [" + def.getParentSkill().getSkillName() + "] trÆ°á»›c!");
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
            throw new AppException(ErrorCode.BAD_REQUEST, "Ká»¹ nÄƒng nÃ y Ä‘Ã£ Ä‘Æ°á»£c má»Ÿ khÃ³a rá»“i!");
        }
        if (c.getSkillPoints() < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "KhÃ´ng Ä‘á»§ Äiá»ƒm ká»¹ nÄƒng (Skill Points)! HÃ£y lÃªn cáº¥p Ä‘á»ƒ nháº­n thÃªm Ä‘iá»ƒm.");
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
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Báº¡n pháº£i má»Ÿ khÃ³a ká»¹ nÄƒng nÃ y trÆ°á»›c khi nÃ¢ng cáº¥p!"));

        NxvSkillDefinitions def = userSkill.getSkill();
        if (userSkill.getSkillLevel() >= def.getMaxSkillLevel()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Ká»¹ nÄƒng [" + def.getSkillName() + "] Ä‘Ã£ Ä‘áº¡t cáº¥p Ä‘á»™ tá»‘i Ä‘a (" + def.getMaxSkillLevel() + ")!");
        }
        if (c.getSkillPoints() < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "KhÃ´ng Ä‘á»§ Äiá»ƒm ká»¹ nÄƒng (Skill Points) Ä‘á»ƒ nÃ¢ng cáº¥p!");
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
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Báº¡n chÆ°a má»Ÿ khÃ³a ká»¹ nÄƒng nÃ y!"));

        NxvSkillDefinitions def = userSkill.getSkill();
        if (def.getSkillType() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "ÄÃ¢y lÃ  Ká»¹ nÄƒng Thá»¥ Ä‘á»™ng (Passive), khÃ´ng thá»ƒ kÃ­ch hoáº¡t thá»§ cÃ´ng!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (userSkill.getCooldownEndsAt() != null && userSkill.getCooldownEndsAt().isAfter(now)) {
            long rem = Duration.between(now, userSkill.getCooldownEndsAt()).getSeconds();
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Ká»¹ nÄƒng [" + def.getSkillName() + "] Ä‘ang há»“i chiÃªu! CÃ²n láº¡i " + rem + " giÃ¢y.");
        }

        if (c.getCurrentMp() < def.getMpCost()) {
            throw new AppException(ErrorCode.INSUFFICIENT_MP,
                    "KhÃ´ng Ä‘á»§ Mana (MP)! Cáº§n " + def.getMpCost() + " MP (Hiá»‡n cÃ³: " + c.getCurrentMp() + " MP).");
        }

        Long targetTaskId = (req != null) ? req.getTargetTaskId() : null;
        NxvTasks targetTask = null;
        String effectDescription;

        String effectCode = (def.getSkillEffect() != null) ? def.getSkillEffect().trim().toUpperCase() : "";
        if ("DASH_24H".equals(effectCode)) {
            // Chuc nang #51: Chieu Luot (Dash) - Gia han Deadline them 24h cho 1 Task
            if (targetTaskId == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, "ChiÃªu LÆ°á»›t (DASH_24H) báº¯t buá»™c pháº£i truyá»n targetTaskId cáº§n gia háº¡n!");
            }
            targetTask = tasksRepository.findById(targetTaskId)
                    .filter(t -> Objects.equals(t.getUser().getUserId(), userId))
                    .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Task cá»§a báº¡n vá»›i ID: " + targetTaskId));

            if (targetTask.getStatus() != 0) {
                throw new AppException(ErrorCode.TASK_ALREADY_PROCESSED, "Chá»‰ cÃ³ thá»ƒ gia háº¡n cho Task Ä‘ang á»Ÿ tráº¡ng thÃ¡i Chá» thá»±c hiá»‡n (Pending)!");
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

            effectDescription = "ÄÃ£ kÃ­ch hoáº¡t [" + def.getSkillName() + "]! Task \"" + targetTask.getTitle() + "\" Ä‘Æ°á»£c gia háº¡n thÃªm 24 giá» (Deadline má»›i: " + newDeadline + ").";
        } else if ("SHIELD_TASK".equals(effectCode)) {
            // Chuc nang #50: Khien Chan (Shield) - Bao ve Task & Nhan vat khong bi tru mau khi tre han
            c.setIsShieldActive(true);
            if (targetTaskId != null) {
                targetTask = tasksRepository.findById(targetTaskId)
                        .filter(t -> Objects.equals(t.getUser().getUserId(), userId))
                        .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Task cá»§a báº¡n vá»›i ID: " + targetTaskId));
                targetTask.setIsShielded(true);
                targetTask.setUpdatedAt(now);
                tasksRepository.save(targetTask);

                taskHistoryRepository.save(NxvTaskHistory.builder()
                        .task(targetTask)
                        .user(c.getUser())
                        .actionType("SKILL_SHIELD")
                        .actionAt(now)
                        .build());

                effectDescription = "ÄÃ£ báº­t [" + def.getSkillName() + "] báº£o vá»‡ Task \"" + targetTask.getTitle() + "\" vÃ  NhÃ¢n váº­t khá»i bá»‹ trá»« mÃ¡u khi trá»… háº¡n!";
            } else {
                effectDescription = "ÄÃ£ báº­t [" + def.getSkillName() + "] trÃªn ngÆ°á»i NhÃ¢n váº­t! Tá»± Ä‘á»™ng cháº·n sÃ¡t thÆ°Æ¡ng cho láº§n trá»… háº¡n tiáº¿p theo.";
            }
        } else {
            effectDescription = "ÄÃ£ kÃ­ch hoáº¡t ká»¹ nÄƒng [" + def.getSkillName() + "] thÃ nh cÃ´ng!";
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
                .title("KÃ­ch hoáº¡t ká»¹ nÄƒng: " + def.getSkillName())
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
            throw new AppException(ErrorCode.BAD_REQUEST, "Báº¡n chÆ°a cá»™ng Ä‘iá»ƒm vÃ o ká»¹ nÄƒng nÃ o Ä‘á»ƒ Reset!");
        }
        if (c.getGold() < RESET_SKILL_TREE_COST_GOLD) {
            throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                    "KhÃ´ng Ä‘á»§ VÃ ng! Cáº§n " + RESET_SKILL_TREE_COST_GOLD + " Gold Ä‘á»ƒ Reset toÃ n bá»™ CÃ¢y ká»¹ nÄƒng.");
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n váº­t!"));
    }
}