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
@Tag(name = "Module 2: NhÃ¢n váº­t, Chá»‰ sá»‘ RPG & ThÃ nh tá»±u (Chá»©c nÄƒng #12 - #21, #101)", description = "Dashboard HP/MP/EXP/Gold, Äá»•i Avatar/Khung tÄ©nh, Danh hiá»‡u, Lá»‹ch sá»­ chá»‰ sá»‘ & Profile Card")
public class NxvCharacterController {

    private final NxvCharacterService characterService;

    @Operation(summary = "[#13] Xem Dashboard tráº¡ng thÃ¡i NhÃ¢n váº­t hiá»‡n táº¡i (HP, MP, EXP, Gold, Base/Effective Stats & Tá»•ng Buff)")
    @GetMapping("/me/dashboard")
    public ApiResponse<CharacterDashboardDto> getMyDashboard() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getDashboard(userId));
    }

    @Operation(summary = "[#12] Äá»•i tÃªn NhÃ¢n váº­t")
    @PutMapping("/me/name")
    public ApiResponse<CharacterDashboardDto> renameCharacter(@Valid @RequestBody UpdateCharacterNameRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Äá»•i tÃªn nhÃ¢n váº­t thÃ nh cÃ´ng!", characterService.renameCharacter(userId, request));
    }

    @Operation(summary = "[#14] Äá»•i Avatar 2D tÄ©nh cá»§a NhÃ¢n váº­t (Tá»« váº­t pháº©m Gacha hoáº·c URL tÄ©nh)")
    @PutMapping("/me/avatar")
    public ApiResponse<CharacterDashboardDto> changeAvatar(@RequestBody ChangeAvatarRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t Avatar 2D tÄ©nh thÃ nh cÃ´ng!", characterService.changeAvatar(userId, request));
    }

    @Operation(summary = "[#18] Thay Ä‘á»•i Khung Avatar tÄ©nh (Avatar Frame)")
    @PutMapping("/me/avatar-frame")
    public ApiResponse<CharacterDashboardDto> changeAvatarFrame(@RequestBody ChangeAvatarFrameRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t Khung Avatar thÃ nh cÃ´ng!", characterService.changeAvatarFrame(userId, request));
    }

    @Operation(summary = "[#19A] Xem danh sÃ¡ch toÃ n bá»™ Danh hiá»‡u (Titles/Badges) vÃ  tráº¡ng thÃ¡i má»Ÿ khÃ³a")
    @GetMapping("/me/titles")
    public ApiResponse<List<TitleDto>> getMyTitles() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getAllTitlesForUser(userId));
    }

    @Operation(summary = "[#19B] Thay Ä‘á»•i Danh hiá»‡u Ä‘ang hiá»ƒn thá»‹ (Equip/Unequip Title)")
    @PutMapping("/me/title")
    public ApiResponse<CharacterDashboardDto> equipTitle(@RequestBody EquipTitleRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t danh hiá»‡u hiá»ƒn thá»‹ thÃ nh cÃ´ng!", characterService.equipTitle(userId, request));
    }

    @Operation(summary = "[#15, #20] Xem báº£ng tÃ­nh toÃ¡n EXP lÅ©y tiáº¿n vÃ  HP/MP gá»‘c theo tá»«ng cáº¥p Ä‘á»™")
    @GetMapping("/level-progression")
    public ApiResponse<List<LevelStepDto>> getLevelProgression(@RequestParam(defaultValue = "20") int maxLevel) {
        return ApiResponse.ok(characterService.getLevelProgressionTable(maxLevel));
    }

    @Operation(summary = "[#15, #16] Äiá»ƒm danh hÃ ng ngÃ y (Daily Check-in) tÄƒng Streak, nháº­n Gold/EXP vÃ  tá»± kiá»ƒm tra Level Up")
    @PostMapping("/me/check-in")
    public ApiResponse<Map<String, Object>> dailyCheckIn() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Äiá»ƒm danh hÃ ng ngÃ y thÃ nh cÃ´ng!", characterService.dailyCheckIn(userId));
    }

    @Operation(summary = "[#17] Xem lá»‹ch sá»­ biáº¿n Ä‘á»™ng chá»‰ sá»‘ (Stat Logs: EXP, Gold, HP, MP, LevelUp)")
    @GetMapping("/me/stat-logs")
    public ApiResponse<List<StatLogDto>> getMyStatLogs(@RequestParam(required = false) String logType) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getStatLogs(userId, logType));
    }

    @Operation(summary = "[#101] Xem danh sÃ¡ch ThÃ nh tá»±u (Achievements) vÃ  tá»± Ä‘á»™ng nháº­n thÆ°á»Ÿng khi Ä‘á»§ Ä‘iá»u kiá»‡n")
    @GetMapping("/me/achievements")
    public ApiResponse<List<AchievementDto>> getMyAchievements() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(characterService.getMyAchievements(userId));
    }

    @Operation(summary = "[#21, #101] Xem Tháº» thÃ´ng tin tÄ©nh (Profile Card) cá»§a ngÆ°á»i chÆ¡i báº¥t ká»³ Ä‘á»ƒ chia sáº» MXH")
    @GetMapping("/profile-card/{targetUserId}")
    public ApiResponse<PublicProfileCardDto> getPublicProfileCard(@PathVariable Long targetUserId) {
        return ApiResponse.ok(characterService.getPublicProfileCard(targetUserId));
    }
}