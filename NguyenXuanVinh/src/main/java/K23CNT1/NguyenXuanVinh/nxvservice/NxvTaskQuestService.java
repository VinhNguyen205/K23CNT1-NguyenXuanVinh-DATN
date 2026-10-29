package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvTaskQuestDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvTaskQuestService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvTasksRepository tasksRepository;
    private final NxvTagsRepository tagsRepository;
    private final NxvTaskTagsRepository taskTagsRepository;
    private final NxvTaskTemplatesRepository taskTemplatesRepository;
    private final NxvTaskHistoryRepository taskHistoryRepository;
    private final NxvEpicQuestsRepository epicQuestsRepository;
    private final NxvSubTasksRepository subTasksRepository;
    private final NxvGPASubjectsRepository gpaSubjectsRepository;
    private final NxvPartiesRepository partiesRepository;
    private final NxvTitlesRepository titlesRepository;
    private final NxvUserTitlesRepository userTitlesRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;
    private final NxvVwOverdueTasksRepository overdueTasksRepository;
    private final NxvCharacterService characterService;
    private final NxvSkillClassService skillClassService;

    public static final int SKIP_TASK_MP_COST = 15;

    // =========================================================================
    // PHAN 1: QUAN LY TAGS & MAU THOI QUEN LAP LAI (#26, #27)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagsRepository.findAll().stream()
                .map(t -> TagDto.builder()
                        .tagId(t.getTagId())
                        .tagName(t.getTagName())
                        .colorHex(t.getColorHex())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public TagDto createTag(CreateTagRequest req) {
        String name = req.getTagName().trim();
        NxvTags tag = tagsRepository.findByTagNameIgnoreCase(name).orElseGet(() ->
                tagsRepository.save(NxvTags.builder()
                        .tagName(name)
                        .colorHex(req.getColorHex() != null && !req.getColorHex().isBlank() ? req.getColorHex().trim() : "#3B82F6")
                        .build())
        );
        return TagDto.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .colorHex(tag.getColorHex())
                .build();
    }

    // Chuc nang #27: Thiet lap lap lai (Recurring Templates) & Sinh Task tu Template
    @Transactional(readOnly = true)
    public List<TaskTemplateDto> getMyTemplates(Long userId) {
        return taskTemplatesRepository.findByUser_UserIdOrderByTemplateIdDesc(userId).stream()
                .map(t -> TaskTemplateDto.builder()
                        .templateId(t.getTemplateId())
                        .title(t.getTitle())
                        .difficulty(t.getDifficulty())
                        .recurringRule(t.getRecurringRule())
                        .isActive(t.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskTemplateDto createTemplate(Long userId, CreateTemplateRequest req) {
        Users user = getUserAlive(userId);
        int diff = normalizeDifficulty(req.getDifficulty());
        NxvTaskTemplates tpl = taskTemplatesRepository.save(NxvTaskTemplates.builder()
                .user(user)
                .title(req.getTitle().trim())
                .difficulty(diff)
                .recurringRule(req.getRecurringRule().trim().toUpperCase())
                .isActive(true)
                .build());

        return TaskTemplateDto.builder()
                .templateId(tpl.getTemplateId())
                .title(tpl.getTitle())
                .difficulty(tpl.getDifficulty())
                .recurringRule(tpl.getRecurringRule())
                .isActive(tpl.getIsActive())
                .build();
    }

    @Transactional
    public TaskDto spawnTaskFromTemplate(Long userId, Long templateId) {
        NxvTaskTemplates tpl = taskTemplatesRepository.findByTemplateIdAndUser_UserId(templateId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy mẫu thói quen lặp lại ID: " + templateId));

        LocalDateTime deadline = LocalDateTime.now().plusDays(1);
        if ("WEEKLY".equalsIgnoreCase(tpl.getRecurringRule())) {
            deadline = LocalDateTime.now().plusDays(7);
        } else if ("MONTHLY".equalsIgnoreCase(tpl.getRecurringRule())) {
            deadline = LocalDateTime.now().plusDays(30);
        }

        return createTask(userId, CreateTaskRequest.builder()
                .title(tpl.getTitle())
                .difficulty(tpl.getDifficulty())
                .deadlineAt(deadline)
                .build());
    }

    // =========================================================================
    // PHAN 2: QUAN LY CORE TASK, KANBAN, COMPLETE, SKIP, UNDO, FREEZE (#22->#26, #31->#41)
    // =========================================================================

    // Chuc nang #22, #25, #26, #38, #39, #40: Them moi Daily Task
    @Transactional
    public TaskDto createTask(Long userId, CreateTaskRequest req) {
        Users user = getUserAlive(userId);
        int diff = normalizeDifficulty(req.getDifficulty());

        // He so thuong/phat mac dinh theo do kho neu nguoi dung khong truyen tay
        int defaultExp = (diff == 3) ? 60 : (diff == 2) ? 35 : 20;
        int defaultGold = (diff == 3) ? 40 : (diff == 2) ? 25 : 15;
        int defaultMp = (diff == 3) ? 15 : (diff == 2) ? 10 : 5;
        int defaultPenaltyHp = (diff == 3) ? 25 : (diff == 2) ? 15 : 10;

        NxvEpicQuests epicQuest = null;
        if (req.getEpicQuestId() != null) {
            epicQuest = epicQuestsRepository.findById(req.getEpicQuestId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Epic Quest ID: " + req.getEpicQuestId()));
        }

        NxvTasks task = NxvTasks.builder()
                .user(user)
                .epicQuest(epicQuest)
                .title(req.getTitle().trim())
                .descriptionMarkdown(req.getDescriptionMarkdown())
                .colorLabel(req.getColorLabel() != null ? req.getColorLabel().trim() : "#3B82F6")
                .difficulty(diff)
                .rewardExp(req.getRewardExp() != null ? req.getRewardExp() : defaultExp)
                .rewardGold(req.getRewardGold() != null ? req.getRewardGold() : defaultGold)
                .rewardMp(req.getRewardMp() != null ? req.getRewardMp() : defaultMp)
                .penaltyHp(req.getPenaltyHp() != null ? req.getPenaltyHp() : defaultPenaltyHp)
                .status(NxvConstants.TASK_PENDING)
                .sortOrder(0)
                .deadlineAt(req.getDeadlineAt() != null ? req.getDeadlineAt() : LocalDateTime.now().plusHours(24))
                .reminderBeforeMins(req.getReminderBeforeMins() != null ? req.getReminderBeforeMins() : 60)
                .isFrozen(false)
                .isShielded(false)
                .undoAvailable(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        task = tasksRepository.save(task);

        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            attachTagsToTask(task, req.getTagIds());
        }

        taskHistoryRepository.save(NxvTaskHistory.builder()
                .task(task)
                .user(user)
                .actionType("CREATE")
                .actionAt(LocalDateTime.now())
                .build());

        return toTaskDto(task);
    }

    // Chuc nang #23, #25, #26, #38, #39, #40: Sua thong tin Task
    @Transactional
    public TaskDto updateTask(Long userId, Long taskId, UpdateTaskRequest req) {
        getUserAlive(userId);
        NxvTasks task = getUserTask(userId, taskId);

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            task.setTitle(req.getTitle().trim());
        }
        if (req.getDescriptionMarkdown() != null) {
            task.setDescriptionMarkdown(req.getDescriptionMarkdown());
        }
        if (req.getColorLabel() != null) {
            task.setColorLabel(req.getColorLabel().trim());
        }
        if (req.getDifficulty() != null) {
            task.setDifficulty(normalizeDifficulty(req.getDifficulty()));
        }
        if (req.getRewardExp() != null) task.setRewardExp(req.getRewardExp());
        if (req.getRewardGold() != null) task.setRewardGold(req.getRewardGold());
        if (req.getRewardMp() != null) task.setRewardMp(req.getRewardMp());
        if (req.getPenaltyHp() != null) task.setPenaltyHp(req.getPenaltyHp());
        if (req.getDeadlineAt() != null) task.setDeadlineAt(req.getDeadlineAt());
        if (req.getReminderBeforeMins() != null) task.setReminderBeforeMins(req.getReminderBeforeMins());

        if (req.getEpicQuestId() != null) {
            NxvEpicQuests eq = epicQuestsRepository.findById(req.getEpicQuestId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Epic Quest ID: " + req.getEpicQuestId()));
            task.setEpicQuest(eq);
        }

        if (req.getTagIds() != null) {
            taskTagsRepository.deleteByTask_TaskId(taskId);
            attachTagsToTask(task, req.getTagIds());
        }

        task.setUpdatedAt(LocalDateTime.now());
        task = tasksRepository.save(task);
        return toTaskDto(task);
    }

    // Chuc nang #24: Xoa / Huy Task
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        NxvTasks task = getUserTask(userId, taskId);
        taskTagsRepository.deleteByTask_TaskId(taskId);
        List<NxvTaskHistory> histories = taskHistoryRepository.findByTask_TaskIdOrderByActionAtDesc(taskId);
        if (!histories.isEmpty()) {
            taskHistoryRepository.deleteAll(histories);
        }
        tasksRepository.delete(task);
    }

    // Chuc nang #33: Loc va Tim kiem Task theo trang thai / Tag / Do kho / Ngay / Tu khoa
    @Transactional(readOnly = true)
    public List<TaskDto> filterMyTasks(Long userId,
                                       Integer status,
                                       Integer tagId,
                                       Integer difficulty,
                                       Long epicQuestId,
                                       LocalDate deadlineDate,
                                       String keyword) {
        List<NxvTasks> tasks = tasksRepository.findByUser_UserIdOrderBySortOrderAscCreatedAtDesc(userId);

        Map<Long, List<TagDto>> taskTagMap = new HashMap<>();
        for (NxvTaskTags tt : taskTagsRepository.findByTask_User_UserId(userId)) {
            Long tid = tt.getTask().getTaskId();
            taskTagMap.computeIfAbsent(tid, k -> new ArrayList<>()).add(TagDto.builder()
                    .tagId(tt.getTag().getTagId())
                    .tagName(tt.getTag().getTagName())
                    .colorHex(tt.getTag().getColorHex())
                    .build());
        }

        return tasks.stream()
                .filter(t -> status == null || Objects.equals(t.getStatus(), status))
                .filter(t -> difficulty == null || Objects.equals(t.getDifficulty(), difficulty))
                .filter(t -> epicQuestId == null || (t.getEpicQuest() != null && Objects.equals(t.getEpicQuest().getEpicQuestId(), epicQuestId)))
                .filter(t -> deadlineDate == null || (t.getDeadlineAt() != null && t.getDeadlineAt().toLocalDate().equals(deadlineDate)))
                .filter(t -> keyword == null || keyword.isBlank() || t.getTitle().toLowerCase().contains(keyword.trim().toLowerCase()))
                .filter(t -> {
                    if (tagId == null) return true;
                    List<TagDto> tgs = taskTagMap.getOrDefault(t.getTaskId(), Collections.emptyList());
                    return tgs.stream().anyMatch(tg -> Objects.equals(tg.getTagId(), tagId));
                })
                .map(t -> toTaskDtoWithTags(t, taskTagMap.getOrDefault(t.getTaskId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    // Chuc nang #31: Check-in hoan thanh Task (Goi Stored Procedure NxvSp_CompleteTask + Tag Mastery + Achievements)
    @Transactional
    public Map<String, Object> completeTask(Long userId, Long taskId) {
        Users user = getUserAlive(userId);
        NxvTasks task = getUserTask(userId, taskId);

        if (Boolean.TRUE.equals(task.getIsFrozen())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Task này đang bị Đóng băng (Paused)! Vui lòng mở đóng băng trước khi hoàn thành.");
        }

        // Goi Stored Procedure NxvSp_CompleteTask (Tinh buff Trang bi + Class + Pet, Level Up, SkillPoints)
        Map<String, Object> spResult = tasksRepository.executeCompleteTaskSp(taskId, userId);

        // Tang diem chuyen mon Tag Mastery (#42)
        List<NxvTags> attachedTags = taskTagsRepository.findByTask_TaskId(taskId).stream()
                .map(NxvTaskTags::getTag)
                .collect(Collectors.toList());
        skillClassService.recordTagMasteryProgress(user, attachedTags);

        // Tu dong kiem tra va mo khoa Thanh tuu FIRST_TASK_DONE (#101)
        List<String> newlyUnlockedAchievements = characterService.evaluateAndUnlockAchievements(userId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("taskId", taskId);
        res.put("spResult", spResult);
        res.put("newlyUnlockedAchievements", newlyUnlockedAchievements);
        res.put("updatedDashboard", characterService.getDashboard(userId));
        return res;
    }

    // Chuc nang #32: Tinh nang Bo qua (Skip) dung diem MP thay vi lam
    @Transactional
    public Map<String, Object> skipTaskWithMp(Long userId, Long taskId) {
        Users user = getUserAlive(userId);
        NxvTasks task = getUserTask(userId, taskId);

        if (task.getStatus() != NxvConstants.TASK_PENDING) {
            throw new AppException(ErrorCode.TASK_ALREADY_PROCESSED, "Chỉ có thể Bỏ qua (Skip) Task đang ở trạng thái Pending!");
        }

        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Nhân vật!"));

        int mpCost = SKIP_TASK_MP_COST * normalizeDifficulty(task.getDifficulty());
        if (c.getCurrentMp() < mpCost) {
            throw new AppException(ErrorCode.INSUFFICIENT_MP,
                    "Không đủ Mana (MP) để Skip Task này! Cần " + mpCost + " MP (Hiện có: " + c.getCurrentMp() + " MP).");
        }

        c.setCurrentMp(c.getCurrentMp() - mpCost);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        task.setStatus(NxvConstants.TASK_SKIPPED);
        task.setUpdatedAt(LocalDateTime.now());
        tasksRepository.save(task);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(user)
                .logType("MP")
                .delta((long) -mpCost)
                .sourceType("SkipTask")
                .sourceId(taskId)
                .createdAt(LocalDateTime.now())
                .build());

        taskHistoryRepository.save(NxvTaskHistory.builder()
                .task(task)
                .user(user)
                .actionType("SKIP_BY_MP")
                .actionAt(LocalDateTime.now())
                .build());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("taskId", taskId);
        res.put("newStatus", "SKIPPED");
        res.put("mpSpent", mpCost);
        res.put("remainingMp", c.getCurrentMp());
        return res;
    }

    // Chuc nang #37: Khoi phuc (Undo) Task vua lo tay check hoan thanh
    @Transactional
    public TaskDto undoCompletedTask(Long userId, Long taskId) {
        Users user = getUserAlive(userId);
        NxvTasks task = getUserTask(userId, taskId);

        if (task.getStatus() != NxvConstants.TASK_COMPLETED || !Boolean.TRUE.equals(task.getUndoAvailable())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Task này không thể Hoàn tác (Chưa hoàn thành hoặc đã hết hiệu lực Undo)!");
        }

        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Nhân vật!"));

        long expRollback = Math.min(c.getCurrentExp(), task.getRewardExp().longValue());
        long goldRollback = Math.min(c.getGold(), task.getRewardGold().longValue());

        c.setCurrentExp(c.getCurrentExp() - expRollback);
        c.setGold(c.getGold() - goldRollback);
        c.setTotalTasksDone(Math.max(0, c.getTotalTasksDone() - 1));
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        task.setStatus(NxvConstants.TASK_PENDING);
        task.setUndoAvailable(false);
        task.setCompletedAt(null);
        task.setUpdatedAt(LocalDateTime.now());
        task = tasksRepository.save(task);

        taskHistoryRepository.save(NxvTaskHistory.builder()
                .task(task)
                .user(user)
                .actionType("UNDO")
                .actionAt(LocalDateTime.now())
                .build());

        return toTaskDto(task);
    }

    // Chuc nang #41: Dong bang / Mo dong bang Task (Pause/Resume)
    @Transactional
    public TaskDto toggleFreezeTask(Long userId, Long taskId) {
        Users user = getUserAlive(userId);
        NxvTasks task = getUserTask(userId, taskId);

        if (task.getStatus() != NxvConstants.TASK_PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể Đóng băng/Mở đóng băng cho Task đang Pending!");
        }

        boolean newFrozenState = !Boolean.TRUE.equals(task.getIsFrozen());
        task.setIsFrozen(newFrozenState);
        task.setUpdatedAt(LocalDateTime.now());
        task = tasksRepository.save(task);

        taskHistoryRepository.save(NxvTaskHistory.builder()
                .task(task)
                .user(user)
                .actionType(newFrozenState ? "FREEZE" : "UNFREEZE")
                .actionAt(LocalDateTime.now())
                .build());

        return toTaskDto(task);
    }

    // Chuc nang #34: Keo tha (Drag & Drop) sap xep uu tien tren Kanban Board
    @Transactional
    public List<TaskDto> reorderKanbanTasks(Long userId, ReorderKanbanRequest req) {
        getUserAlive(userId);
        for (KanbanOrderItem item : req.getItems()) {
            tasksRepository.findByTaskIdAndUser_UserId(item.getTaskId(), userId).ifPresent(t -> {
                t.setSortOrder(item.getSortOrder());
                t.setUpdatedAt(LocalDateTime.now());
                tasksRepository.save(t);
            });
        }
        return filterMyTasks(userId, null, null, null, null, null, null);
    }

    // Chuc nang #35: Xem lich su cac Task da lam
    @Transactional(readOnly = true)
    public List<TaskHistoryDto> getMyTaskHistory(Long userId) {
        return taskHistoryRepository.findTop100ByUser_UserIdOrderByActionAtDesc(userId).stream()
                .map(h -> TaskHistoryDto.builder()
                        .historyId(h.getHistoryId())
                        .taskId(h.getTask().getTaskId())
                        .taskTitle(h.getTask().getTitle())
                        .actionType(h.getActionType())
                        .actionAt(h.getActionAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Chuc nang #36 & #40: Chay Cronjob quet Task qua han (NxvSp_CronDailyDeadlineCheck) & Gui thong bao nhac nho
    @Transactional
    public Map<String, Object> executeNightlyDeadlineCronAndReminders() {
        long overdueCountBefore = overdueTasksRepository.count();
        tasksRepository.executeCronDailyDeadlineCheckSp();

        // Quet gui thong bao nhac nho cho cac task sap toi Deadline (#40)
        LocalDateTime now = LocalDateTime.now();
        int remindersCreated = 0;
        List<NxvTasks> pendingTasks = tasksRepository.findByStatusAndIsFrozenFalseAndDeadlineAtIsNotNull(NxvConstants.TASK_PENDING);
        for (NxvTasks t : pendingTasks) {
            if (t.getReminderBeforeMins() != null && t.getReminderBeforeMins() > 0 && t.getDeadlineAt().isAfter(now)) {
                long minsLeft = Duration.between(now, t.getDeadlineAt()).toMinutes();
                if (minsLeft <= t.getReminderBeforeMins()) {
                    notificationsRepository.save(NxvNotifications.builder()
                            .user(t.getUser())
                            .title("Sắp hết hạn Task: " + t.getTitle())
                            .content("Task \"" + t.getTitle() + "\" chỉ còn " + minsLeft + " phút nữa là tới hạn! Hãy hoàn thành hoặc dùng Chiêu Lướt/Khiên Chắn ngay.")
                            .notificationType("REMINDER")
                            .isRead(false)
                            .createdAt(now)
                            .build());
                    remindersCreated++;
                }
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("executedAt", now);
        res.put("overdueTasksProcessed", overdueCountBefore);
        res.put("remindersSent", remindersCreated);
        return res;
    }

    // =========================================================================
    // PHAN 3: EPIC QUEST, SUB-TASKS & THUAT TOAN DIEM GPA DAI HOC (#28, #29, #30)
    // =========================================================================

    // Chuc nang #28 & #30: Tao Epic Quest & Cai dat phan thuong
    @Transactional
    public EpicQuestDetailDto createEpicQuest(Long userId, CreateEpicQuestRequest req) {
        Users user = getUserAlive(userId);
        NxvParties party = null;
        if (req.getPartyId() != null) {
            party = partiesRepository.findById(req.getPartyId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Party ID: " + req.getPartyId()));
        }

        boolean isGpa = Boolean.TRUE.equals(req.getIsGpaQuest());
        BigDecimal targetGpa = isGpa
                ? (req.getTargetGpa() != null ? req.getTargetGpa() : new BigDecimal("3.20"))
                : null;

        NxvEpicQuests eq = NxvEpicQuests.builder()
                .user(user)
                .party(party)
                .title(req.getTitle().trim())
                .isGpaQuest(isGpa)
                .targetGpa(targetGpa)
                .rewardExp(req.getRewardExp() != null ? req.getRewardExp() : 500)
                .rewardGold(req.getRewardGold() != null ? req.getRewardGold() : 300)
                .status(0)
                .createdAt(LocalDateTime.now())
                .build();
        eq = epicQuestsRepository.save(eq);
        return getEpicQuestDetail(userId, eq.getEpicQuestId());
    }

    @Transactional(readOnly = true)
    public List<EpicQuestDetailDto> getMyEpicQuests(Long userId) {
        return epicQuestsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(eq -> buildEpicQuestDetailDto(eq, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public EpicQuestDetailDto getEpicQuestDetail(Long userId, Long epicQuestId) {
        NxvEpicQuests eq = getUserEpicQuest(userId, epicQuestId);
        BigDecimal calcGpa = null;
        if (Boolean.TRUE.equals(eq.getIsGpaQuest())) {
            calcGpa = epicQuestsRepository.executeCalculateGpaSp(epicQuestId);
        }
        return buildEpicQuestDetailDto(eq, calcGpa);
    }

    // Chuc nang #29: Chia nho Epic Quest thanh cac Check-list / Sub-tasks
    @Transactional
    public EpicQuestDetailDto addSubTask(Long userId, Long epicQuestId, CreateSubTaskRequest req) {
        getUserAlive(userId);
        NxvEpicQuests eq = getUserEpicQuest(userId, epicQuestId);
        subTasksRepository.save(NxvSubTasks.builder()
                .epicQuest(eq)
                .title(req.getTitle().trim())
                .isDone(false)
                .createdAt(LocalDateTime.now())
                .build());
        return getEpicQuestDetail(userId, epicQuestId);
    }

    @Transactional
    public EpicQuestDetailDto toggleSubTask(Long userId, Long subTaskId) {
        getUserAlive(userId);
        NxvSubTasks st = subTasksRepository.findBySubTaskIdAndEpicQuest_User_UserId(subTaskId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Sub-task ID: " + subTaskId));
        st.setIsDone(!Boolean.TRUE.equals(st.getIsDone()));
        subTasksRepository.save(st);
        return getEpicQuestDetail(userId, st.getEpicQuest().getEpicQuestId());
    }

    // Chuc nang #28: Them mon hoc & Goi Stored Procedure NxvSp_CalculateGPA (Tinh A tu 8.6, loai GDQP & diem F)
    @Transactional
    public EpicQuestDetailDto addGpaSubjectAndCalculate(Long userId, Long epicQuestId, AddGpaSubjectRequest req) {
        getUserAlive(userId);
        NxvEpicQuests eq = getUserEpicQuest(userId, epicQuestId);
        eq.setIsGpaQuest(true);
        if (eq.getTargetGpa() == null) {
            eq.setTargetGpa(new BigDecimal("3.20"));
        }
        epicQuestsRepository.save(eq);

        gpaSubjectsRepository.save(NxvGPASubjects.builder()
                .epicQuest(eq)
                .subjectName(req.getSubjectName().trim())
                .credits(req.getCredits() != null && req.getCredits() > 0 ? req.getCredits() : 3)
                .score(req.getScore())
                .isExcluded(false)
                .build());

        BigDecimal calculatedGpa = epicQuestsRepository.executeCalculateGpaSp(epicQuestId);
        return buildEpicQuestDetailDto(eq, calculatedGpa);
    }

    @Transactional
    public EpicQuestDetailDto deleteGpaSubject(Long userId, Long subjectId) {
        NxvGPASubjects subj = gpaSubjectsRepository.findBySubjectIdAndEpicQuest_User_UserId(subjectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy môn học ID: " + subjectId));
        Long eqId = subj.getEpicQuest().getEpicQuestId();
        gpaSubjectsRepository.delete(subj);
        return getEpicQuestDetail(userId, eqId);
    }

    // Chuc nang #28 & #30: Hoan thanh Epic Quest -> Nhan Vang, EXP va Danh hieu doc quyen "Huyen Thoai GPA 3.6+"
    @Transactional
    public Map<String, Object> completeEpicQuest(Long userId, Long epicQuestId) {
        Users user = getUserAlive(userId);
        NxvEpicQuests eq = getUserEpicQuest(userId, epicQuestId);
        if (eq.getStatus() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Epic Quest này đã được xử lý trước đó!");
        }

        BigDecimal gpa = null;
        if (Boolean.TRUE.equals(eq.getIsGpaQuest())) {
            gpa = epicQuestsRepository.executeCalculateGpaSp(epicQuestId);
            BigDecimal target = eq.getTargetGpa() != null ? eq.getTargetGpa() : new BigDecimal("3.20");
            if (gpa == null || gpa.compareTo(target) < 0) {
                throw new AppException(ErrorCode.BAD_REQUEST,
                        "GPA hiện tại (" + (gpa != null ? gpa : "0.00") + ") chưa đạt mục tiêu đề ra (" + target + ")!");
            }
        } else {
            List<NxvSubTasks> subs = subTasksRepository.findByEpicQuest_EpicQuestIdOrderByCreatedAtAsc(epicQuestId);
            boolean anyIncomplete = subs.stream().anyMatch(st -> !Boolean.TRUE.equals(st.getIsDone()));
            if (anyIncomplete) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Vui lòng hoàn thành toàn bộ các Check-list (Sub-tasks) trước khi chốt Epic Quest!");
            }
        }

        eq.setStatus(1);
        epicQuestsRepository.save(eq);

        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Nhân vật!"));

        long goldReward = eq.getRewardGold() != null ? eq.getRewardGold() : 300L;
        long expReward = eq.getRewardExp() != null ? eq.getRewardExp() : 500L;

        c.setGold(c.getGold() + goldReward);
        statLogsRepository.save(NxvStatLogs.builder()
                .user(user)
                .logType("Gold")
                .delta(goldReward)
                .sourceType("EpicQuestComplete")
                .sourceId(epicQuestId)
                .createdAt(LocalDateTime.now())
                .build());

        int levelsGained = characterService.addExpAndCheckLevelUp(c, expReward, "EpicQuestComplete", epicQuestId);

        // Trao Danh hieu doc quyen "Huyen Thoai GPA 3.6+" (TitleID = 3) neu dat GPAQuest
        String exclusiveTitleGranted = null;
        if (Boolean.TRUE.equals(eq.getIsGpaQuest())) {
            NxvTitles exclusiveTitle = titlesRepository.findById(3).orElse(null);
            if (exclusiveTitle != null && !userTitlesRepository.existsByUser_UserIdAndTitle_TitleId(userId, 3)) {
                userTitlesRepository.save(NxvUserTitles.builder()
                        .user(user)
                        .title(exclusiveTitle)
                        .unlockedAt(LocalDateTime.now())
                        .build());
                exclusiveTitleGranted = exclusiveTitle.getTitleName();
            }
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("epicQuestId", epicQuestId);
        res.put("title", eq.getTitle());
        res.put("finalGpa", gpa);
        res.put("goldGained", goldReward);
        res.put("expGained", expReward);
        res.put("levelsGained", levelsGained);
        res.put("exclusiveTitleGranted", exclusiveTitleGranted);
        return res;
    }

    // --- Helpers ---
    private void attachTagsToTask(NxvTasks task, List<Integer> tagIds) {
        Set<Integer> uniqueIds = new LinkedHashSet<>(tagIds);
        for (Integer tagId : uniqueIds) {
            tagsRepository.findById(tagId).ifPresent(tag ->
                    taskTagsRepository.save(NxvTaskTags.builder()
                            .task(task)
                            .tag(tag)
                            .build())
            );
        }
    }

    private TaskDto toTaskDto(NxvTasks t) {
        List<TagDto> tags = taskTagsRepository.findByTask_TaskId(t.getTaskId()).stream()
                .map(tt -> TagDto.builder()
                        .tagId(tt.getTag().getTagId())
                        .tagName(tt.getTag().getTagName())
                        .colorHex(tt.getTag().getColorHex())
                        .build())
                .collect(Collectors.toList());
        return toTaskDtoWithTags(t, tags);
    }

    private TaskDto toTaskDtoWithTags(NxvTasks t, List<TagDto> tags) {
        String diffLabel = switch (t.getDifficulty() != null ? t.getDifficulty() : 1) {
            case 3 -> "Hard (Khó)";
            case 2 -> "Normal (Trung bình)";
            default -> "Easy (Dễ)";
        };
        String statusLabel = switch (t.getStatus() != null ? t.getStatus() : 0) {
            case 1 -> "COMPLETED (Đã hoàn thành)";
            case 2 -> "FAILED (Quá hạn / Thất bại)";
            case 3 -> "SKIPPED (Đã bỏ qua bằng MP)";
            default -> "PENDING (Đang chờ)";
        };

        return TaskDto.builder()
                .taskId(t.getTaskId())
                .userId(t.getUser().getUserId())
                .epicQuestId(t.getEpicQuest() != null ? t.getEpicQuest().getEpicQuestId() : null)
                .epicQuestTitle(t.getEpicQuest() != null ? t.getEpicQuest().getTitle() : null)
                .partyTaskId(t.getPartyTask() != null ? t.getPartyTask().getTaskId() : null)
                .title(t.getTitle())
                .descriptionMarkdown(t.getDescriptionMarkdown())
                .colorLabel(t.getColorLabel())
                .difficulty(t.getDifficulty())
                .difficultyLabel(diffLabel)
                .rewardExp(t.getRewardExp())
                .rewardGold(t.getRewardGold())
                .rewardMp(t.getRewardMp())
                .penaltyHp(t.getPenaltyHp())
                .status(t.getStatus())
                .statusLabel(statusLabel)
                .sortOrder(t.getSortOrder())
                .deadlineAt(t.getDeadlineAt())
                .reminderBeforeMins(t.getReminderBeforeMins())
                .isFrozen(t.getIsFrozen())
                .isShielded(t.getIsShielded())
                .undoAvailable(t.getUndoAvailable())
                .completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .tags(tags)
                .build();
    }

    private EpicQuestDetailDto buildEpicQuestDetailDto(NxvEpicQuests eq, BigDecimal preCalculatedGpa) {
        List<NxvSubTasks> subs = subTasksRepository.findByEpicQuest_EpicQuestIdOrderByCreatedAtAsc(eq.getEpicQuestId());
        int doneCount = (int) subs.stream().filter(st -> Boolean.TRUE.equals(st.getIsDone())).count();

        List<SubTaskDto> subDtos = subs.stream()
                .map(st -> SubTaskDto.builder()
                        .subTaskId(st.getSubTaskId())
                        .epicQuestId(eq.getEpicQuestId())
                        .title(st.getTitle())
                        .isDone(st.getIsDone())
                        .createdAt(st.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<NxvGPASubjects> subjects = gpaSubjectsRepository.findByEpicQuest_EpicQuestIdOrderBySubjectIdAsc(eq.getEpicQuestId());
        List<GpaSubjectDto> subjectDtos = subjects.stream()
                .map(s -> {
                    double sc = s.getScore() != null ? s.getScore().doubleValue() : 0.0;
                    String letter = sc >= 8.6 ? "A" : sc >= 8.0 ? "B+" : sc >= 7.0 ? "B" : sc >= 6.5 ? "C+"
                            : sc >= 5.5 ? "C" : sc >= 5.0 ? "D+" : sc >= 4.0 ? "D" : "F";
                    BigDecimal gp4 = sc >= 8.6 ? new BigDecimal("4.0") : sc >= 8.0 ? new BigDecimal("3.5")
                            : sc >= 7.0 ? new BigDecimal("3.0") : sc >= 6.5 ? new BigDecimal("2.5")
                            : sc >= 5.5 ? new BigDecimal("2.0") : sc >= 5.0 ? new BigDecimal("1.5")
                            : sc >= 4.0 ? new BigDecimal("1.0") : BigDecimal.ZERO;

                    return GpaSubjectDto.builder()
                            .subjectId(s.getSubjectId())
                            .epicQuestId(eq.getEpicQuestId())
                            .subjectName(s.getSubjectName())
                            .credits(s.getCredits())
                            .score(s.getScore())
                            .letterGrade(letter)
                            .gradePoint4(gp4)
                            .isExcluded(s.getIsExcluded())
                            .excludeReason(s.getExcludeReason())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal gpa = preCalculatedGpa;
        if (gpa == null && Boolean.TRUE.equals(eq.getIsGpaQuest()) && !subjects.isEmpty()) {
            gpa = epicQuestsRepository.executeCalculateGpaSp(eq.getEpicQuestId()); if (gpa != null) gpa = gpa.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        boolean reached = (gpa != null && eq.getTargetGpa() != null && gpa.compareTo(eq.getTargetGpa()) >= 0);

        return EpicQuestDetailDto.builder()
                .epicQuestId(eq.getEpicQuestId())
                .userId(eq.getUser().getUserId())
                .partyId(eq.getParty() != null ? eq.getParty().getPartyId() : null)
                .title(eq.getTitle())
                .isGpaQuest(eq.getIsGpaQuest())
                .targetGpa(eq.getTargetGpa())
                .currentCalculatedGpa(gpa)
                .isTargetGpaReached(reached)
                .rewardExp(eq.getRewardExp())
                .rewardGold(eq.getRewardGold())
                .status(eq.getStatus())
                .completedSubTasks(doneCount)
                .totalSubTasks(subs.size())
                .createdAt(eq.getCreatedAt())
                .subTasks(subDtos)
                .gpaSubjects(subjectDtos)
                .build();
    }

    private int normalizeDifficulty(Integer d) {
        if (d == null || d < 1) return 1;
        return Math.min(d, 3);
    }

    private Users getUserAlive(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
        return u;
    }

    private NxvTasks getUserTask(Long userId, Long taskId) {
        return tasksRepository.findByTaskIdAndUser_UserId(taskId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, "Không tìm thấy Task ID: " + taskId));
    }

    private NxvEpicQuests getUserEpicQuest(Long userId, Long epicQuestId) {
        return epicQuestsRepository.findById(epicQuestId)
                .filter(eq -> Objects.equals(eq.getUser().getUserId(), userId))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Epic Quest ID: " + epicQuestId));
    }
}
