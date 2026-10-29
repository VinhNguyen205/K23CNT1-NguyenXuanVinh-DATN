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