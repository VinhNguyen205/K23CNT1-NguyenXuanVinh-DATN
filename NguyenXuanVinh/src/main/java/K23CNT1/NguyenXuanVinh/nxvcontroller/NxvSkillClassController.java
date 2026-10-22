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
@Tag(name = "Module 4: CÃ¢y Ká»¹ NÄƒng & Lá»›p NhÃ¢n Váº­t (Chá»©c nÄƒng #42 - #56)", description = "Theo dÃµi Tag Mastery, Äá» xuáº¥t & Äá»•i Class Ä‘á»™ng, Má»Ÿ khÃ³a/NÃ¢ng cáº¥p/Reset CÃ¢y ká»¹ nÄƒng, DÃ¹ng chiÃªu KhiÃªn Cháº¯n & LÆ°á»›t 24h")
public class NxvSkillClassController {

    private final NxvSkillClassService skillClassService;

    @Operation(summary = "[#42, #43, #52] Xem Ä‘iá»ƒm chuyÃªn mÃ´n theo Tag, Ä‘á» xuáº¥t chuyá»ƒn Class Ä‘á»™ng vÃ  chá»‰ sá»‘ Buff thá»¥ Ä‘á»™ng tá»« Class")
    @GetMapping("/classes/overview")
    public ApiResponse<ClassOverviewDto> getClassOverview() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getClassAndMasteryOverview(userId));
    }

    @Operation(summary = "[#53] Chá»n hoáº·c Äá»•i Class nhÃ¢n váº­t (Tech Mage, Physical Knight...)")
    @PostMapping("/classes/change")
    public ApiResponse<ClassOverviewDto> changeClass(@Valid @RequestBody ChangeClassRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Chuyá»ƒn Ä‘á»•i Class nhÃ¢n váº­t thÃ nh cÃ´ng!", skillClassService.changeCharacterClass(userId, request));
    }

    @Operation(summary = "[#53B] Xem lá»‹ch sá»­ chuyá»ƒn Ä‘á»•i Class cá»§a nhÃ¢n váº­t")
    @GetMapping("/classes/history")
    public ApiResponse<List<ClassChangeLogDto>> getClassHistory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getClassChangeLogs(userId));
    }

    @Operation(summary = "[#44, #45, #49] Xem giao diá»‡n CÃ¢y ká»¹ nÄƒng (Skill Tree) dáº¡ng nhÃ¡nh tÄ©nh, SkillPoints vÃ  thá»i gian Cooldown")
    @GetMapping("/tree")
    public ApiResponse<SkillTreeOverviewDto> getSkillTree() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getSkillTreeOverview(userId));
    }

    @Operation(summary = "[#46] Má»Ÿ khÃ³a ká»¹ nÄƒng má»›i báº±ng Skill Point (Kiá»ƒm tra cáº¥p Ä‘á»™ yÃªu cáº§u & ká»¹ nÄƒng nhÃ¡nh cha)")
    @PostMapping("/skills/{skillId}/unlock")
    public ApiResponse<SkillTreeOverviewDto> unlockSkill(@PathVariable Integer skillId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Má»Ÿ khÃ³a ká»¹ nÄƒng má»›i thÃ nh cÃ´ng!", skillClassService.unlockSkill(userId, skillId));
    }

    @Operation(summary = "[#47] NÃ¢ng cáº¥p (Upgrade) cáº¥p Ä‘á»™ ká»¹ nÄƒng báº±ng Skill Point (Giáº£m thá»i gian há»“i chiÃªu)")
    @PostMapping("/skills/{skillId}/upgrade")
    public ApiResponse<SkillTreeOverviewDto> upgradeSkill(@PathVariable Integer skillId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("NÃ¢ng cáº¥p ká»¹ nÄƒng thÃ nh cÃ´ng!", skillClassService.upgradeSkill(userId, skillId));
    }

    @Operation(summary = "[#48, #50, #51, #56] KÃ­ch hoáº¡t Ká»¹ nÄƒng chá»§ Ä‘á»™ng (Trá»« MP): ChiÃªu LÆ°á»›t (DASH_24H gia háº¡n 24h) & KhiÃªn Cháº¯n (SHIELD_TASK)")
    @PostMapping("/skills/{skillId}/cast")
    public ApiResponse<Map<String, Object>> castSkill(@PathVariable Integer skillId,
                                                      @RequestBody(required = false) CastSkillRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("KÃ­ch hoáº¡t ká»¹ nÄƒng thÃ nh cÃ´ng!", skillClassService.castActiveSkill(userId, skillId, request));
    }

    @Operation(summary = "[#54] Reset toÃ n bá»™ CÃ¢y ká»¹ nÄƒng Ä‘á»ƒ hoÃ n láº¡i toÃ n bá»™ Skill Points (Tá»‘n VÃ ng)")
    @PostMapping("/tree/reset")
    public ApiResponse<SkillTreeOverviewDto> resetSkillTree() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ Reset CÃ¢y ká»¹ nÄƒng vÃ  hoÃ n tráº£ toÃ n bá»™ Ä‘iá»ƒm ká»¹ nÄƒng!", skillClassService.resetSkillTree(userId));
    }

    @Operation(summary = "[#55] Xem lá»‹ch sá»­ sá»­ dá»¥ng ká»¹ nÄƒng (Skill Usage Logs)")
    @GetMapping("/skills/usage-logs")
    public ApiResponse<List<SkillUsageLogDto>> getSkillUsageLogs() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(skillClassService.getSkillUsageLogs(userId));
    }
}