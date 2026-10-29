package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvCharacterDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvCharacterService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
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