package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvInventoryDtos.ItemDetailDto;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvSystemAdminDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.NxvAIConfig;
import K23CNT1.NguyenXuanVinh.nxventity.NxvGachaBanners;
import K23CNT1.NguyenXuanVinh.nxventity.NxvTags;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvSystemAdminService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin-system")
@RequiredArgsConstructor
@Tag(name = "Module 8: Quáº£n trá»‹ viÃªn Admin Dashboard & Há»‡ thá»‘ng (Chá»©c nÄƒng #103 - #115)", description = "Thá»‘ng kÃª tá»•ng quan, Ban/Unban Users, PhÃ¢n quyá»n, Cáº¥u hÃ¬nh Ä‘á»™ng Gacha/Item/Banner/Pháº¡t HP, Cronjobs, Global Notifications, Log VÃ ng, Report & AI Config")
public class NxvSystemAdminController {

    private final NxvSystemAdminService systemAdminService;

    @Operation(summary = "[#103] Dashboard thá»‘ng kÃª tá»•ng quan toÃ n há»‡ thá»‘ng (Users, Tasks, Gacha, Marketplace, Doanh thu Há»“i sinh VNÄ)")
    @GetMapping("/dashboard/summary")
    public ApiResponse<AdminDashboardSummaryDto> getDashboardSummary() {
        return ApiResponse.ok(systemAdminService.getSystemDashboardSummary());
    }

    @Operation(summary = "[#104A] Xem danh sÃ¡ch toÃ n bá»™ tÃ i khoáº£n ngÆ°á»i dÃ¹ng trong báº£ng Users")
    @GetMapping("/users")
    public ApiResponse<List<AdminUserRowDto>> getAllUsers() {
        return ApiResponse.ok(systemAdminService.getAllUsersForAdmin());
    }

    @Operation(summary = "[#104B] KhÃ³a / Má»Ÿ khÃ³a tÃ i khoáº£n ngÆ°á»i dÃ¹ng (Ban / Unban báº£ng Users)")
    @PostMapping("/users/{targetUserId}/ban-toggle")
    public ApiResponse<AdminUserRowDto> toggleBanUser(@PathVariable Long targetUserId,
                                                      @RequestParam(defaultValue = "true") boolean lock) {
        return ApiResponse.ok(lock ? "ÄÃ£ khÃ³a (Ban) tÃ i khoáº£n thÃ nh cÃ´ng!" : "ÄÃ£ má»Ÿ khÃ³a (Unban) tÃ i khoáº£n thÃ nh cÃ´ng!",
                systemAdminService.toggleBanUser(targetUserId, lock));
    }

    @Operation(summary = "[#113] PhÃ¢n quyá»n Admin Ä‘a cáº¥p (0 = User, 1 = Moderator, 2 = SuperAdmin)")
    @PutMapping("/users/{targetUserId}/role")
    public ApiResponse<AdminUserRowDto> changeUserRole(@PathVariable Long targetUserId,
                                                       @Valid @RequestBody ChangeUserRoleRequest request) {
        return ApiResponse.ok("Cáº­p nháº­t phÃ¢n quyá»n tÃ i khoáº£n thÃ nh cÃ´ng!", systemAdminService.changeUserRole(targetUserId, request));
    }

    // --- ITEM, BANNER & GACHA DYNAMIC CONFIG (#105, #106, #107) ---
    @Operation(summary = "[#106A] ThÃªm má»›i Váº­t pháº©m vÃ o danh má»¥c (Gáº¯n icon 2D tÄ©nh, setup chá»‰ sá»‘ Buff)")
    @PostMapping("/items")
    public ApiResponse<ItemDetailDto> createItem(@Valid @RequestBody UpsertItemRequest request) {
        return ApiResponse.ok("ThÃªm má»›i váº­t pháº©m vÃ o há»‡ thá»‘ng thÃ nh cÃ´ng!", systemAdminService.createCatalogItem(request));
    }

    @Operation(summary = "[#106B] Chá»‰nh sá»­a thÃ´ng sá»‘ Buff / GiÃ¡ bÃ¡n / áº¢nh 2D tÄ©nh cá»§a Váº­t pháº©m")
    @PutMapping("/items/{itemId}")
    public ApiResponse<ItemDetailDto> updateItem(@PathVariable Integer itemId,
                                                 @RequestBody UpsertItemRequest request) {
        return ApiResponse.ok("Cáº­p nháº­t váº­t pháº©m thÃ nh cÃ´ng!", systemAdminService.updateCatalogItem(itemId, request));
    }

    @Operation(summary = "[#107A] Táº¡o má»›i Banner Há»™p mÃ¹ (Gáº¯n áº£nh tÄ©nh, GiÃ¡ má»Ÿ, Má»‘c báº£o hiá»ƒm Pity)")
    @PostMapping("/gacha/banners")
    public ApiResponse<NxvGachaBanners> createBanner(@Valid @RequestBody UpsertGachaBannerRequest request) {
        return ApiResponse.ok("Táº¡o má»›i Banner Há»™p mÃ¹ thÃ nh cÃ´ng!", systemAdminService.createGachaBanner(request));
    }

    @Operation(summary = "[#107B, #105] Cáº­p nháº­t Banner Há»™p mÃ¹ (Äá»•i giÃ¡ quay, chá»‰nh sá»­a má»‘c Pity, Ä‘Ã³ng/má»Ÿ Banner)")
    @PutMapping("/gacha/banners/{bannerId}")
    public ApiResponse<NxvGachaBanners> updateBanner(@PathVariable Integer bannerId,
                                                     @RequestBody UpsertGachaBannerRequest request) {
        return ApiResponse.ok("Cáº­p nháº­t Banner Há»™p mÃ¹ thÃ nh cÃ´ng!", systemAdminService.updateGachaBanner(bannerId, request));
    }

    @Operation(summary = "[#105] Cáº¥u hÃ¬nh Ä‘á»™ng Tá»· lá»‡ rá»›t Ä‘á»“ (Drop Rate) vÃ  Váº­t pháº©m Featured cho Banner Gacha")
    @PostMapping("/gacha/drop-rates")
    public ApiResponse<Map<String, Object>> upsertDropRate(@Valid @RequestBody UpsertDropRateRequest request) {
        return ApiResponse.ok("Cáº­p nháº­t tá»· lá»‡ rá»›t Ä‘á»“ Gacha thÃ nh cÃ´ng!", systemAdminService.upsertGachaDropRate(request));
    }

    // --- DIFFICULTY PENALTY & CRONJOB MONITOR (#108, #109) ---
    @Operation(summary = "[#108A] Xem cáº¥u hÃ¬nh há»‡ sá»‘ Pháº¡t HP vÃ  ThÆ°á»Ÿng máº·c Ä‘á»‹nh cho tá»«ng Ä‘á»™ khÃ³ cá»§a Task")
    @GetMapping("/config/difficulty-penalties")
    public ApiResponse<DifficultyPenaltyConfigDto> getDifficultyConfig() {
        return ApiResponse.ok(systemAdminService.getDifficultyPenaltyConfig());
    }

    @Operation(summary = "[#108B] Cáº­p nháº­t há»‡ sá»‘ Pháº¡t (LÆ°á»£ng HP bá»‹ trá»«) máº·c Ä‘á»‹nh cho tá»«ng Ä‘á»™ khÃ³ cá»§a Task")
    @PutMapping("/config/difficulty-penalties")
    public ApiResponse<Map<String, Object>> updateDifficultyConfig(
            @RequestBody DifficultyPenaltyConfigDto request,
            @RequestParam(defaultValue = "false") boolean applyToExistingPendingTasks) {
        return ApiResponse.ok("ÄÃ£ cáº­p nháº­t há»‡ sá»‘ pháº¡t Ä‘á»™ khÃ³!", systemAdminService.updateDifficultyPenaltyConfig(request, applyToExistingPendingTasks));
    }

    @Operation(summary = "[#109] Theo dÃµi vÃ  kÃ­ch hoáº¡t cháº¡y kiá»ƒm tra toÃ n bá»™ cÃ¡c Cronjob ngáº§m (Trá»« mÃ¡u Task quÃ¡ háº¡n & Pet ngá»§ Ä‘Ã´ng)")
    @PostMapping("/cronjobs/run-all")
    public ApiResponse<Map<String, Object>> runAllCronjobs() {
        return ApiResponse.ok("ÄÃ£ thá»±c thi toÃ n bá»™ Cronjob há»‡ thá»‘ng!", systemAdminService.triggerAllSystemCronjobsManually());
    }

    // --- GLOBAL NOTIFICATIONS (#110) ---
    @Operation(summary = "[#110A] Gá»­i thÃ´ng bÃ¡o (Global Notification Ä‘áº©y Ä‘áº¿n toÃ n bá»™ Client náº¿u targetUserId = null)")
    @PostMapping("/notifications/send")
    public ApiResponse<NotificationDto> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        return ApiResponse.ok("ÄÃ£ phÃ¡t thÃ´ng bÃ¡o thÃ nh cÃ´ng!", systemAdminService.sendNotification(request));
    }

    @Operation(summary = "[#110B] Xem danh sÃ¡ch thÃ´ng bÃ¡o cá»§a tÃ´i vÃ  Global Notifications")
    @GetMapping("/notifications/me")
    public ApiResponse<List<NotificationDto>> getMyNotifications() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(systemAdminService.getMyAndGlobalNotifications(userId));
    }

    @Operation(summary = "[#110C] ÄÃ¡nh dáº¥u Ä‘Ã£ Ä‘á»c thÃ´ng bÃ¡o")
    @PatchMapping("/notifications/{notificationId}/read")
    public ApiResponse<NotificationDto> markRead(@PathVariable Long notificationId) {
        return ApiResponse.ok(systemAdminService.markNotificationRead(notificationId));
    }

    // --- SYSTEM LOGS, TAGS, REPORTS & AI CONFIG (#111, #112, #114, #115) ---
    @Operation(summary = "[#111] Xem Log há»‡ thá»‘ng (Lá»‹ch sá»­ náº¡p/trá»« VÃ ng, EXP, HP toÃ n Server Ä‘á»ƒ phÃ¡t hiá»‡n bug)")
    @GetMapping("/logs/stat-logs")
    public ApiResponse<List<Map<String, Object>>> getGlobalStatLogs(@RequestParam(required = false) String logType) {
        return ApiResponse.ok(systemAdminService.getGlobalServerStatLogs(logType));
    }

    @Operation(summary = "[#121-ADMIN] Äá»‘i soÃ¡t toÃ n bá»™ HÃ³a Ä‘Æ¡n tiá»n tháº­t (Billing) & Lá»‹ch sá»­ Webhook NgÃ¢n hÃ ng")
    @GetMapping("/logs/billing-webhooks")
    public ApiResponse<Map<String, Object>> getBillingAndWebhookLogs() {
        return ApiResponse.ok(systemAdminService.getAllBillingAndWebhookLogsForAdmin());
    }

    @Operation(summary = "[#112A] Cáº­p nháº­t Tháº» chuyÃªn mÃ´n (Tag) máº·c Ä‘á»‹nh cá»§a há»‡ thá»‘ng")
    @PutMapping("/tags/{tagId}")
    public ApiResponse<NxvTags> updateTag(@PathVariable Integer tagId,
                                          @RequestParam String tagName,
                                          @RequestParam(required = false) String colorHex) {
        return ApiResponse.ok("Cáº­p nháº­t Tag thÃ nh cÃ´ng!", systemAdminService.updateSystemTag(tagId, tagName, colorHex));
    }

    @Operation(summary = "[#112B] XÃ³a Tháº» chuyÃªn mÃ´n (Tag)")
    @DeleteMapping("/tags/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Integer tagId) {
        systemAdminService.deleteSystemTag(tagId);
        return ApiResponse.ok("ÄÃ£ xÃ³a Tag thÃ nh cÃ´ng!", null);
    }

    @Operation(summary = "[#114A] NgÆ°á»i chÆ¡i gá»­i Khiáº¿u náº¡i / BÃ¡o cÃ¡o lá»—i (Report)")
    @PostMapping("/reports")
    public ApiResponse<UserReportDto> submitReport(@Valid @RequestBody CreateUserReportRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ gá»­i bÃ¡o cÃ¡o lÃªn Ban quáº£n trá»‹!", systemAdminService.submitUserReport(userId, request));
    }

    @Operation(summary = "[#114B] Admin xem danh sÃ¡ch Khiáº¿u náº¡i / Report tá»« ngÆ°á»i dÃ¹ng")
    @GetMapping("/reports")
    public ApiResponse<List<UserReportDto>> getAllReports(@RequestParam(required = false) Integer status) {
        return ApiResponse.ok(systemAdminService.getAllUserReports(status));
    }

    @Operation(summary = "[#114C] Admin duyá»‡t vÃ  xá»­ lÃ½ Khiáº¿u náº¡i / Report (1 = Resolved, 2 = Rejected)")
    @PutMapping("/reports/{reportId}/resolve")
    public ApiResponse<UserReportDto> resolveReport(@PathVariable Long reportId,
                                                    @Valid @RequestBody ResolveReportRequest request) {
        return ApiResponse.ok("ÄÃ£ xá»­ lÃ½ bÃ¡o cÃ¡o khiáº¿u náº¡i!", systemAdminService.resolveUserReport(reportId, request));
    }

    @Operation(summary = "[#115A] Xem cáº¥u hÃ¬nh API káº¿t ná»‘i AI Chatbot hiá»‡n táº¡i")
    @GetMapping("/ai-config")
    public ApiResponse<List<NxvAIConfig>> getAiConfigs() {
        return ApiResponse.ok(systemAdminService.getAllAiConfigs());
    }

    @Operation(summary = "[#115B] Cáº­p nháº­t cáº¥u hÃ¬nh API káº¿t ná»‘i AI Chatbot (Äá»•i Token / Model / System Prompt)")
    @PutMapping("/ai-config")
    public ApiResponse<NxvAIConfig> upsertAiConfig(@Valid @RequestBody UpsertAiConfigRequest request) {
        return ApiResponse.ok("Cáº­p nháº­t cáº¥u hÃ¬nh AI Chatbot thÃ nh cÃ´ng!", systemAdminService.upsertAiConfig(request));
    }
}