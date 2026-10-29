package K23CNT1.NguyenXuanVinh.nxvdto;

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