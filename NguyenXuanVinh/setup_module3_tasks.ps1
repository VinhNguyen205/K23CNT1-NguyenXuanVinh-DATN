# ==============================================================================
# TASKTALE / LIFERPG - MODULE 3 AUTOMATED GENERATOR (TASKS, KANBAN, CRON & GPA)
# Hoan thien tron bo 20 Chuc nang #22 -> #41: Daily Tasks, Tags, Recurring,
# Epic Quests, University GPA Calculator (NxvSp_CalculateGPA), Sub-tasks,
# Complete Task (NxvSp_CompleteTask), Skip by MP, Undo, Freeze, Kanban Reorder,
# Reminder Notifications & Nightly Cronjob (NxvSp_CronDailyDeadlineCheck)
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

Write-Host "==> Dang sinh Module 3 (Chuc nang #22 -> #41: Core Task, Kanban, Cronjob & Epic Quest GPA) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 3
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvTagsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NxvTagsRepository extends JpaRepository<NxvTags, Integer> {
    Optional<NxvTags> findByTagNameIgnoreCase(String tagName);
    boolean existsByTagNameIgnoreCase(String tagName);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvTaskTagsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvTaskTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NxvTaskTagsRepository extends JpaRepository<NxvTaskTags, Long> {
    List<NxvTaskTags> findByTask_TaskId(Long taskId);
    List<NxvTaskTags> findByTask_User_UserId(Long userId);

    @Transactional
    void deleteByTask_TaskId(Long taskId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvTaskTemplatesRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvTaskTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvTaskTemplatesRepository extends JpaRepository<NxvTaskTemplates, Long> {
    List<NxvTaskTemplates> findByUser_UserIdOrderByTemplateIdDesc(Long userId);
    Optional<NxvTaskTemplates> findByTemplateIdAndUser_UserId(Long templateId, Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvTaskHistoryRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvTaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvTaskHistoryRepository extends JpaRepository<NxvTaskHistory, Long> {
    List<NxvTaskHistory> findTop100ByUser_UserIdOrderByActionAtDesc(Long userId);
    List<NxvTaskHistory> findByTask_TaskIdOrderByActionAtDesc(Long taskId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvSubTasksRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvSubTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvSubTasksRepository extends JpaRepository<NxvSubTasks, Long> {
    List<NxvSubTasks> findByEpicQuest_EpicQuestIdOrderByCreatedAtAsc(Long epicQuestId);
    Optional<NxvSubTasks> findBySubTaskIdAndEpicQuest_User_UserId(Long subTaskId, Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvGPASubjectsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvGPASubjects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvGPASubjectsRepository extends JpaRepository<NxvGPASubjects, Long> {
    List<NxvGPASubjects> findByEpicQuest_EpicQuestIdOrderBySubjectIdAsc(Long epicQuestId);
    Optional<NxvGPASubjects> findBySubjectIdAndEpicQuest_User_UserId(Long subjectId, Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvTasksRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface NxvTasksRepository extends JpaRepository<NxvTasks, Long> {
    List<NxvTasks> findByUser_UserIdOrderBySortOrderAscCreatedAtDesc(Long userId);
    Optional<NxvTasks> findByTaskIdAndUser_UserId(Long taskId, Long userId);
    List<NxvTasks> findByPartyTask_TaskId(Long partyTaskId);
    List<NxvTasks> findByStatusAndIsFrozenFalseAndDeadlineAtIsNotNull(Integer status);

    @Query(value = "EXEC NxvSp_CompleteTask @TaskID = :taskId, @UserID = :userId", nativeQuery = true)
    Map<String, Object> executeCompleteTaskSp(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_CronDailyDeadlineCheck", nativeQuery = true)
    void executeCronDailyDeadlineCheckSp();
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 3 (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvTaskQuestDtos.java" @"
package $basePkg.nxvdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NxvTaskQuestDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TagDto {
        private Integer tagId;
        private String tagName;
        private String colorHex;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateTagRequest {
        @NotBlank(message = "Tên Tag không được để trống")
        private String tagName;
        private String colorHex;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateTaskRequest {
        @NotBlank(message = "Tiêu đề Task không được để trống")
        private String title;

        private String descriptionMarkdown;
        private String colorLabel;
        private Integer difficulty; // 1=Easy, 2=Normal, 3=Hard
        private Integer rewardExp;
        private Integer rewardGold;
        private Integer rewardMp;
        private Integer penaltyHp;
        private LocalDateTime deadlineAt;
        private Integer reminderBeforeMins;
        private Long epicQuestId;
        private List<Integer> tagIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateTaskRequest {
        private String title;
        private String descriptionMarkdown;
        private String colorLabel;
        private Integer difficulty;
        private Integer rewardExp;
        private Integer rewardGold;
        private Integer rewardMp;
        private Integer penaltyHp;
        private LocalDateTime deadlineAt;
        private Integer reminderBeforeMins;
        private Long epicQuestId;
        private List<Integer> tagIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TaskDto {
        private Long taskId;
        private Long userId;
        private Long epicQuestId;
        private String epicQuestTitle;
        private Long partyTaskId;
        private String title;
        private String descriptionMarkdown;
        private String colorLabel;
        private Integer difficulty;
        private String difficultyLabel;
        private Integer rewardExp;
        private Integer rewardGold;
        private Integer rewardMp;
        private Integer penaltyHp;
        private Integer status; // 0=Pending, 1=Completed, 2=Failed, 3=Skipped
        private String statusLabel;
        private Integer sortOrder;
        private LocalDateTime deadlineAt;
        private Integer reminderBeforeMins;
        private Boolean isFrozen;
        private Boolean isShielded;
        private Boolean undoAvailable;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<TagDto> tags;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class KanbanOrderItem {
        @NotNull
        private Long taskId;
        @NotNull
        private Integer sortOrder;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReorderKanbanRequest {
        @NotNull(message = "Danh sách thứ tự task không được để trống")
        private List<KanbanOrderItem> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TaskTemplateDto {
        private Long templateId;
        private String title;
        private Integer difficulty;
        private String recurringRule; // DAILY, WEEKLY, MONTHLY
        private Boolean isActive;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateTemplateRequest {
        @NotBlank(message = "Tiêu đề mẫu thói quen không được để trống")
        private String title;
        private Integer difficulty;
        @NotBlank(message = "Quy tắc lặp lại (DAILY, WEEKLY, MONTHLY) không được để trống")
        private String recurringRule;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TaskHistoryDto {
        private Long historyId;
        private Long taskId;
        private String taskTitle;
        private String actionType;
        private LocalDateTime actionAt;
    }

    // --- EPIC QUEST & GPA DTOS ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateEpicQuestRequest {
        @NotBlank(message = "Tiêu đề Epic Quest không được để trống")
        private String title;
        private Boolean isGpaQuest;
        private BigDecimal targetGpa;
        private Integer rewardExp;
        private Integer rewardGold;
        private Long partyId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubTaskDto {
        private Long subTaskId;
        private Long epicQuestId;
        private String title;
        private Boolean isDone;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateSubTaskRequest {
        @NotBlank(message = "Tiêu đề Sub-task không được để trống")
        private String title;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GpaSubjectDto {
        private Long subjectId;
        private Long epicQuestId;
        private String subjectName;
        private Integer credits;
        private BigDecimal score;
        private String letterGrade;
        private BigDecimal gradePoint4;
        private Boolean isExcluded;
        private String excludeReason;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddGpaSubjectRequest {
        @NotBlank(message = "Tên môn học không được để trống")
        private String subjectName;
        private Integer credits;
        @NotNull(message = "Điểm tổng kết hệ 10 không được để trống")
        private BigDecimal score;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EpicQuestDetailDto {
        private Long epicQuestId;
        private Long userId;
        private Long partyId;
        private String title;
        private Boolean isGpaQuest;
        private BigDecimal targetGpa;
        private BigDecimal currentCalculatedGpa;
        private Boolean isTargetGpaReached;
        private Integer rewardExp;
        private Integer rewardGold;
        private Integer status;
        private Integer completedSubTasks;
        private Integer totalSubTasks;
        private LocalDateTime createdAt;
        private List<SubTaskDto> subTasks;
        private List<GpaSubjectDto> gpaSubjects;
    }
}
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 3 (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvTaskQuestService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvTaskQuestDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvenums.NxvConstants;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
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
            gpa = epicQuestsRepository.executeCalculateGpaSp(eq.getEpicQuestId());
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
"@

# ==============================================================================
# 4. SINH CRONJOB SCHEDULER CHAY NGAM MOI DEM (#36) (nxvutils)
# ==============================================================================
Write-JavaFile "$baseDir\nxvutils\NxvTaskDeadlineScheduler.java" @"
package $basePkg.nxvutils;

import $basePkg.nxvservice.NxvTaskQuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class NxvTaskDeadlineScheduler {

    private final NxvTaskQuestService taskQuestService;

    // Chuc nang #36: Tu dong chay vao 00:05 moi dem de danh dau Fail cac Task qua han & tru HP
    @Scheduled(cron = "0 5 0 * * *")
    public void runNightlyDeadlineCheck() {
        log.info("[TaskTale Cronjob] Bat dau quet cac Task qua han va tru HP...");
        var result = taskQuestService.executeNightlyDeadlineCronAndReminders();
        log.info("[TaskTale Cronjob] Hoan tat: {}", result);
    }
}
"@

# ==============================================================================
# 5. SINH REST CONTROLLER CHO MODULE 3 (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvTaskQuestController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvTaskQuestDtos.*;
import $basePkg.nxvservice.NxvTaskQuestService;
import $basePkg.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tasks-quests")
@RequiredArgsConstructor
@Tag(name = "Module 3: Quản lý Nhiệm vụ, Kanban & Epic Quest GPA (Chức năng #22 - #41)", description = "CRUD Task, Tag, Recurring, Check-in NxvSp_CompleteTask, Skip MP, Undo, Freeze, Kanban, Cronjob & Epic Quest GPA")
public class NxvTaskQuestController {

    private final NxvTaskQuestService taskQuestService;

    // --- TAGS & RECURRING TEMPLATES (#26, #27) ---
    @Operation(summary = "[#26A] Xem danh sách toàn bộ Thẻ chuyên môn (Tags: Backend, Thể thao, Tiếng Anh...)")
    @GetMapping("/tags")
    public ApiResponse<List<TagDto>> getAllTags() {
        return ApiResponse.ok(taskQuestService.getAllTags());
    }

    @Operation(summary = "[#26B] Tạo mới Thẻ chuyên môn (Tag)")
    @PostMapping("/tags")
    public ApiResponse<TagDto> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.ok("Tạo thẻ Tag thành công!", taskQuestService.createTag(request));
    }

    @Operation(summary = "[#27A] Xem danh sách Mẫu thói quen lặp lại (Recurring Templates)")
    @GetMapping("/templates")
    public ApiResponse<List<TaskTemplateDto>> getMyTemplates() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getMyTemplates(userId));
    }

    @Operation(summary = "[#27B] Thiết lập Mẫu thói quen lặp lại hàng ngày/tuần/tháng")
    @PostMapping("/templates")
    public ApiResponse<TaskTemplateDto> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Tạo mẫu thói quen lặp lại thành công!", taskQuestService.createTemplate(userId, request));
    }

    @Operation(summary = "[#27C] Sinh Task mới từ Mẫu thói quen lặp lại")
    @PostMapping("/templates/{templateId}/spawn")
    public ApiResponse<TaskDto> spawnFromTemplate(@PathVariable Long templateId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã sinh công việc mới từ mẫu thói quen!", taskQuestService.spawnTaskFromTemplate(userId, templateId));
    }

    // --- CORE TASKS & KANBAN (#22->#26, #31->#41) ---
    @Operation(summary = "[#22, #25, #26, #38, #39, #40] Thêm mới công việc hàng ngày (Daily Task) với độ khó, Tag, Deadline, Markdown & Nhãn màu")
    @PostMapping("/tasks")
    public ApiResponse<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Tạo nhiệm vụ mới thành công!", taskQuestService.createTask(userId, request));
    }

    @Operation(summary = "[#23, #25, #26, #38, #39, #40] Sửa thông tin Task")
    @PutMapping("/tasks/{taskId}")
    public ApiResponse<TaskDto> updateTask(@PathVariable Long taskId, @RequestBody UpdateTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật nhiệm vụ thành công!", taskQuestService.updateTask(userId, taskId, request));
    }

    @Operation(summary = "[#24] Xóa / Hủy Task")
    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        taskQuestService.deleteTask(userId, taskId);
        return ApiResponse.ok("Đã xóa nhiệm vụ thành công!", null);
    }

    @Operation(summary = "[#33] Lọc và Tìm kiếm Task theo Trạng thái, Tag, Độ khó, EpicQuest, Ngày Deadline hoặc Từ khóa")
    @GetMapping("/tasks")
    public ApiResponse<List<TaskDto>> filterMyTasks(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer tagId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Long epicQuestId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineDate,
            @RequestParam(required = false) String keyword) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.filterMyTasks(userId, status, tagId, difficulty, epicQuestId, deadlineDate, keyword));
    }

    @Operation(summary = "[#31] Check-in Hoàn thành Task (Gọi NxvSp_CompleteTask cộng EXP/Gold/MP, tự động Level Up & tăng Tag Mastery)")
    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<Map<String, Object>> completeTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Hoàn thành nhiệm vụ và nhận thưởng thành công!", taskQuestService.completeTask(userId, taskId));
    }

    @Operation(summary = "[#32] Bỏ qua (Skip) Task bằng điểm Mana (MP) thay vì làm")
    @PostMapping("/tasks/{taskId}/skip")
    public ApiResponse<Map<String, Object>> skipTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã dùng MP để bỏ qua nhiệm vụ!", taskQuestService.skipTaskWithMp(userId, taskId));
    }

    @Operation(summary = "[#37] Khôi phục (Undo) Task vừa lỡ tay bấm hoàn thành")
    @PostMapping("/tasks/{taskId}/undo")
    public ApiResponse<TaskDto> undoTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã hoàn tác (Undo) nhiệm vụ về trạng thái chờ!", taskQuestService.undoCompletedTask(userId, taskId));
    }

    @Operation(summary = "[#41] Đóng băng / Mở đóng băng Task (Pause/Resume) khi có việc đột xuất")
    @PostMapping("/tasks/{taskId}/toggle-freeze")
    public ApiResponse<TaskDto> toggleFreeze(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã cập nhật trạng thái đóng băng của nhiệm vụ!", taskQuestService.toggleFreezeTask(userId, taskId));
    }

    @Operation(summary = "[#34] Kéo thả (Drag & Drop) sắp xếp thứ tự ưu tiên trên bảng Kanban")
    @PutMapping("/tasks/kanban-reorder")
    public ApiResponse<List<TaskDto>> reorderKanban(@Valid @RequestBody ReorderKanbanRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật thứ tự Kanban thành công!", taskQuestService.reorderKanbanTasks(userId, request));
    }

    @Operation(summary = "[#35] Xem lịch sử các thao tác Task đã thực hiện")
    @GetMapping("/tasks/history")
    public ApiResponse<List<TaskHistoryDto>> getTaskHistory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getMyTaskHistory(userId));
    }

    @Operation(summary = "[#36, #40] Kích hoạt chạy Cronjob quét Task quá hạn (NxvSp_CronDailyDeadlineCheck) & gửi thông báo nhắc nhở")
    @PostMapping("/cron/trigger-deadline-check")
    public ApiResponse<Map<String, Object>> triggerCronCheck() {
        return ApiResponse.ok("Đã thực thi Cronjob quét Deadline và trừ máu!", taskQuestService.executeNightlyDeadlineCronAndReminders());
    }

    // --- EPIC QUEST & UNIVERSITY GPA (#28, #29, #30) ---
    @Operation(summary = "[#28, #30] Tạo Dự án lớn / Epic Quest (Hỗ trợ mục tiêu GPA đại học & cài đặt phần thưởng)")
    @PostMapping("/epic-quests")
    public ApiResponse<EpicQuestDetailDto> createEpicQuest(@Valid @RequestBody CreateEpicQuestRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Tạo Epic Quest thành công!", taskQuestService.createEpicQuest(userId, request));
    }

    @Operation(summary = "[#28B] Xem danh sách các Epic Quest của tôi")
    @GetMapping("/epic-quests")
    public ApiResponse<List<EpicQuestDetailDto>> getMyEpicQuests() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getMyEpicQuests(userId));
    }

    @Operation(summary = "[#28C] Xem chi tiết Epic Quest, tiến độ Sub-tasks và kết quả tính GPA")
    @GetMapping("/epic-quests/{epicQuestId}")
    public ApiResponse<EpicQuestDetailDto> getEpicQuestDetail(@PathVariable Long epicQuestId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getEpicQuestDetail(userId, epicQuestId));
    }

    @Operation(summary = "[#29A] Chia nhỏ Epic Quest thành các Check-list / Sub-tasks")
    @PostMapping("/epic-quests/{epicQuestId}/sub-tasks")
    public ApiResponse<EpicQuestDetailDto> addSubTask(@PathVariable Long epicQuestId,
                                                      @Valid @RequestBody CreateSubTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Thêm Sub-task vào Epic Quest thành công!", taskQuestService.addSubTask(userId, epicQuestId, request));
    }

    @Operation(summary = "[#29B] Tích chọn hoàn thành / bỏ chọn Sub-task")
    @PatchMapping("/sub-tasks/{subTaskId}/toggle")
    public ApiResponse<EpicQuestDetailDto> toggleSubTask(@PathVariable Long subTaskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật trạng thái Sub-task thành công!", taskQuestService.toggleSubTask(userId, subTaskId));
    }

    @Operation(summary = "[#28D] Thêm môn học vào GPA Quest & gọi NxvSp_CalculateGPA (Điểm >= 8.6 là A, tự loại GDQP và điểm F)")
    @PostMapping("/epic-quests/{epicQuestId}/gpa-subjects")
    public ApiResponse<EpicQuestDetailDto> addGpaSubject(@PathVariable Long epicQuestId,
                                                         @Valid @RequestBody AddGpaSubjectRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã thêm môn học và tự động tính lại GPA tích lũy!", taskQuestService.addGpaSubjectAndCalculate(userId, epicQuestId, request));
    }

    @Operation(summary = "[#28E] Xóa môn học khỏi GPA Quest")
    @DeleteMapping("/gpa-subjects/{subjectId}")
    public ApiResponse<EpicQuestDetailDto> deleteGpaSubject(@PathVariable Long subjectId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã xóa môn học và tính lại GPA!", taskQuestService.deleteGpaSubject(userId, subjectId));
    }

    @Operation(summary = "[#28F, #30] Hoàn thành Epic Quest để nhận Vàng, EXP và Danh hiệu độc quyền")
    @PostMapping("/epic-quests/{epicQuestId}/complete")
    public ApiResponse<Map<String, Object>> completeEpicQuest(@PathVariable Long epicQuestId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Chúc mừng bạn đã chinh phục Epic Quest thành công!", taskQuestService.completeEpicQuest(userId, epicQuestId));
    }
}
"@

Write-Host "==> HOAN TAT SINH MODULE 3 (20 CHUC NANG #22 -> #41: CORE TASK, KANBAN, CRONJOB & EPIC QUEST GPA) THANH CONG!" -ForegroundColor Green