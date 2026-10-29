package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvSkillClassDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvSkillClassService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
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