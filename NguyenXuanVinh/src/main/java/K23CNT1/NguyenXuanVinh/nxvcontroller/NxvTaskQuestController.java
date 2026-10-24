package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvTaskQuestDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvTaskQuestService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
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
@Tag(name = "Module 3: Quáº£n lÃ½ Nhiá»‡m vá»¥, Kanban & Epic Quest GPA (Chá»©c nÄƒng #22 - #41)", description = "CRUD Task, Tag, Recurring, Check-in NxvSp_CompleteTask, Skip MP, Undo, Freeze, Kanban, Cronjob & Epic Quest GPA")
public class NxvTaskQuestController {

    private final NxvTaskQuestService taskQuestService;

    // --- TAGS & RECURRING TEMPLATES (#26, #27) ---
    @Operation(summary = "[#26A] Xem danh sÃ¡ch toÃ n bá»™ Tháº» chuyÃªn mÃ´n (Tags: Backend, Thá»ƒ thao, Tiáº¿ng Anh...)")
    @GetMapping("/tags")
    public ApiResponse<List<TagDto>> getAllTags() {
        return ApiResponse.ok(taskQuestService.getAllTags());
    }

    @Operation(summary = "[#26B] Táº¡o má»›i Tháº» chuyÃªn mÃ´n (Tag)")
    @PostMapping("/tags")
    public ApiResponse<TagDto> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.ok("Táº¡o tháº» Tag thÃ nh cÃ´ng!", taskQuestService.createTag(request));
    }

    @Operation(summary = "[#27A] Xem danh sÃ¡ch Máº«u thÃ³i quen láº·p láº¡i (Recurring Templates)")
    @GetMapping("/templates")
    public ApiResponse<List<TaskTemplateDto>> getMyTemplates() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getMyTemplates(userId));
    }

    @Operation(summary = "[#27B] Thiáº¿t láº­p Máº«u thÃ³i quen láº·p láº¡i hÃ ng ngÃ y/tuáº§n/thÃ¡ng")
    @PostMapping("/templates")
    public ApiResponse<TaskTemplateDto> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Táº¡o máº«u thÃ³i quen láº·p láº¡i thÃ nh cÃ´ng!", taskQuestService.createTemplate(userId, request));
    }

    @Operation(summary = "[#27C] Sinh Task má»›i tá»« Máº«u thÃ³i quen láº·p láº¡i")
    @PostMapping("/templates/{templateId}/spawn")
    public ApiResponse<TaskDto> spawnFromTemplate(@PathVariable Long templateId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ sinh cÃ´ng viá»‡c má»›i tá»« máº«u thÃ³i quen!", taskQuestService.spawnTaskFromTemplate(userId, templateId));
    }

    // --- CORE TASKS & KANBAN (#22->#26, #31->#41) ---
    @Operation(summary = "[#22, #25, #26, #38, #39, #40] ThÃªm má»›i cÃ´ng viá»‡c hÃ ng ngÃ y (Daily Task) vá»›i Ä‘á»™ khÃ³, Tag, Deadline, Markdown & NhÃ£n mÃ u")
    @PostMapping("/tasks")
    public ApiResponse<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Táº¡o nhiá»‡m vá»¥ má»›i thÃ nh cÃ´ng!", taskQuestService.createTask(userId, request));
    }

    @Operation(summary = "[#23, #25, #26, #38, #39, #40] Sá»­a thÃ´ng tin Task")
    @PutMapping("/tasks/{taskId}")
    public ApiResponse<TaskDto> updateTask(@PathVariable Long taskId, @RequestBody UpdateTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t nhiá»‡m vá»¥ thÃ nh cÃ´ng!", taskQuestService.updateTask(userId, taskId, request));
    }

    @Operation(summary = "[#24] XÃ³a / Há»§y Task")
    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        taskQuestService.deleteTask(userId, taskId);
        return ApiResponse.ok("ÄÃ£ xÃ³a nhiá»‡m vá»¥ thÃ nh cÃ´ng!", null);
    }

    @Operation(summary = "[#33] Lá»c vÃ  TÃ¬m kiáº¿m Task theo Tráº¡ng thÃ¡i, Tag, Äá»™ khÃ³, EpicQuest, NgÃ y Deadline hoáº·c Tá»« khÃ³a")
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

    @Operation(summary = "[#31] Check-in HoÃ n thÃ nh Task (Gá»i NxvSp_CompleteTask cá»™ng EXP/Gold/MP, tá»± Ä‘á»™ng Level Up & tÄƒng Tag Mastery)")
    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<Map<String, Object>> completeTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("HoÃ n thÃ nh nhiá»‡m vá»¥ vÃ  nháº­n thÆ°á»Ÿng thÃ nh cÃ´ng!", taskQuestService.completeTask(userId, taskId));
    }

    @Operation(summary = "[#32] Bá» qua (Skip) Task báº±ng Ä‘iá»ƒm Mana (MP) thay vÃ¬ lÃ m")
    @PostMapping("/tasks/{taskId}/skip")
    public ApiResponse<Map<String, Object>> skipTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ dÃ¹ng MP Ä‘á»ƒ bá» qua nhiá»‡m vá»¥!", taskQuestService.skipTaskWithMp(userId, taskId));
    }

    @Operation(summary = "[#37] KhÃ´i phá»¥c (Undo) Task vá»«a lá»¡ tay báº¥m hoÃ n thÃ nh")
    @PostMapping("/tasks/{taskId}/undo")
    public ApiResponse<TaskDto> undoTask(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ hoÃ n tÃ¡c (Undo) nhiá»‡m vá»¥ vá» tráº¡ng thÃ¡i chá»!", taskQuestService.undoCompletedTask(userId, taskId));
    }

    @Operation(summary = "[#41] ÄÃ³ng bÄƒng / Má»Ÿ Ä‘Ã³ng bÄƒng Task (Pause/Resume) khi cÃ³ viá»‡c Ä‘á»™t xuáº¥t")
    @PostMapping("/tasks/{taskId}/toggle-freeze")
    public ApiResponse<TaskDto> toggleFreeze(@PathVariable Long taskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i Ä‘Ã³ng bÄƒng cá»§a nhiá»‡m vá»¥!", taskQuestService.toggleFreezeTask(userId, taskId));
    }

    @Operation(summary = "[#34] KÃ©o tháº£ (Drag & Drop) sáº¯p xáº¿p thá»© tá»± Æ°u tiÃªn trÃªn báº£ng Kanban")
    @PutMapping("/tasks/kanban-reorder")
    public ApiResponse<List<TaskDto>> reorderKanban(@Valid @RequestBody ReorderKanbanRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t thá»© tá»± Kanban thÃ nh cÃ´ng!", taskQuestService.reorderKanbanTasks(userId, request));
    }

    @Operation(summary = "[#35] Xem lá»‹ch sá»­ cÃ¡c thao tÃ¡c Task Ä‘Ã£ thá»±c hiá»‡n")
    @GetMapping("/tasks/history")
    public ApiResponse<List<TaskHistoryDto>> getTaskHistory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getMyTaskHistory(userId));
    }

    @Operation(summary = "[#36, #40] KÃ­ch hoáº¡t cháº¡y Cronjob quÃ©t Task quÃ¡ háº¡n (NxvSp_CronDailyDeadlineCheck) & gá»­i thÃ´ng bÃ¡o nháº¯c nhá»Ÿ")
    @PostMapping("/cron/trigger-deadline-check")
    public ApiResponse<Map<String, Object>> triggerCronCheck() {
        return ApiResponse.ok("ÄÃ£ thá»±c thi Cronjob quÃ©t Deadline vÃ  trá»« mÃ¡u!", taskQuestService.executeNightlyDeadlineCronAndReminders());
    }

    // --- EPIC QUEST & UNIVERSITY GPA (#28, #29, #30) ---
    @Operation(summary = "[#28, #30] Táº¡o Dá»± Ã¡n lá»›n / Epic Quest (Há»— trá»£ má»¥c tiÃªu GPA Ä‘áº¡i há»c & cÃ i Ä‘áº·t pháº§n thÆ°á»Ÿng)")
    @PostMapping("/epic-quests")
    public ApiResponse<EpicQuestDetailDto> createEpicQuest(@Valid @RequestBody CreateEpicQuestRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Táº¡o Epic Quest thÃ nh cÃ´ng!", taskQuestService.createEpicQuest(userId, request));
    }

    @Operation(summary = "[#28B] Xem danh sÃ¡ch cÃ¡c Epic Quest cá»§a tÃ´i")
    @GetMapping("/epic-quests")
    public ApiResponse<List<EpicQuestDetailDto>> getMyEpicQuests() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getMyEpicQuests(userId));
    }

    @Operation(summary = "[#28C] Xem chi tiáº¿t Epic Quest, tiáº¿n Ä‘á»™ Sub-tasks vÃ  káº¿t quáº£ tÃ­nh GPA")
    @GetMapping("/epic-quests/{epicQuestId}")
    public ApiResponse<EpicQuestDetailDto> getEpicQuestDetail(@PathVariable Long epicQuestId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(taskQuestService.getEpicQuestDetail(userId, epicQuestId));
    }

    @Operation(summary = "[#29A] Chia nhá» Epic Quest thÃ nh cÃ¡c Check-list / Sub-tasks")
    @PostMapping("/epic-quests/{epicQuestId}/sub-tasks")
    public ApiResponse<EpicQuestDetailDto> addSubTask(@PathVariable Long epicQuestId,
                                                      @Valid @RequestBody CreateSubTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ThÃªm Sub-task vÃ o Epic Quest thÃ nh cÃ´ng!", taskQuestService.addSubTask(userId, epicQuestId, request));
    }

    @Operation(summary = "[#29B] TÃ­ch chá»n hoÃ n thÃ nh / bá» chá»n Sub-task")
    @PatchMapping("/sub-tasks/{subTaskId}/toggle")
    public ApiResponse<EpicQuestDetailDto> toggleSubTask(@PathVariable Long subTaskId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t tráº¡ng thÃ¡i Sub-task thÃ nh cÃ´ng!", taskQuestService.toggleSubTask(userId, subTaskId));
    }

    @Operation(summary = "[#28D] ThÃªm mÃ´n há»c vÃ o GPA Quest & gá»i NxvSp_CalculateGPA (Äiá»ƒm >= 8.6 lÃ  A, tá»± loáº¡i GDQP vÃ  Ä‘iá»ƒm F)")
    @PostMapping("/epic-quests/{epicQuestId}/gpa-subjects")
    public ApiResponse<EpicQuestDetailDto> addGpaSubject(@PathVariable Long epicQuestId,
                                                         @Valid @RequestBody AddGpaSubjectRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ thÃªm mÃ´n há»c vÃ  tá»± Ä‘á»™ng tÃ­nh láº¡i GPA tÃ­ch lÅ©y!", taskQuestService.addGpaSubjectAndCalculate(userId, epicQuestId, request));
    }

    @Operation(summary = "[#28E] XÃ³a mÃ´n há»c khá»i GPA Quest")
    @DeleteMapping("/gpa-subjects/{subjectId}")
    public ApiResponse<EpicQuestDetailDto> deleteGpaSubject(@PathVariable Long subjectId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ xÃ³a mÃ´n há»c vÃ  tÃ­nh láº¡i GPA!", taskQuestService.deleteGpaSubject(userId, subjectId));
    }

    @Operation(summary = "[#28F, #30] HoÃ n thÃ nh Epic Quest Ä‘á»ƒ nháº­n VÃ ng, EXP vÃ  Danh hiá»‡u Ä‘á»™c quyá»n")
    @PostMapping("/epic-quests/{epicQuestId}/complete")
    public ApiResponse<Map<String, Object>> completeEpicQuest(@PathVariable Long epicQuestId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ChÃºc má»«ng báº¡n Ä‘Ã£ chinh phá»¥c Epic Quest thÃ nh cÃ´ng!", taskQuestService.completeEpicQuest(userId, epicQuestId));
    }
}