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
@Tag(name = "Module 8: Quản trị viên Admin Dashboard & Hệ thống (Chức năng #103 - #115)", description = "Thống kê tổng quan, Ban/Unban Users, Phân quyền, Cấu hình động Gacha/Item/Banner/Phạt HP, Cronjobs, Global Notifications, Log Vàng, Report & AI Config")
public class NxvSystemAdminController {

    private final NxvSystemAdminService systemAdminService;

    @Operation(summary = "[#103] Dashboard thống kê tổng quan toàn hệ thống (Users, Tasks, Gacha, Marketplace, Doanh thu Hồi sinh VNĐ)")
    @GetMapping("/dashboard/summary")
    public ApiResponse<AdminDashboardSummaryDto> getDashboardSummary() {
        return ApiResponse.ok(systemAdminService.getSystemDashboardSummary());
    }

    @Operation(summary = "[#104A] Xem danh sách toàn bộ tài khoản người dùng trong bảng Users")
    @GetMapping("/users")
    public ApiResponse<List<AdminUserRowDto>> getAllUsers() {
        return ApiResponse.ok(systemAdminService.getAllUsersForAdmin());
    }

    @Operation(summary = "[#104B] Khóa / Mở khóa tài khoản người dùng (Ban / Unban bảng Users)")
    @PostMapping("/users/{targetUserId}/ban-toggle")
    public ApiResponse<AdminUserRowDto> toggleBanUser(@PathVariable Long targetUserId,
                                                      @RequestParam(defaultValue = "true") boolean lock) {
        return ApiResponse.ok(lock ? "Đã khóa (Ban) tài khoản thành công!" : "Đã mở khóa (Unban) tài khoản thành công!",
                systemAdminService.toggleBanUser(targetUserId, lock));
    }

    @Operation(summary = "[#113] Phân quyền Admin đa cấp (0 = User, 1 = Moderator, 2 = SuperAdmin)")
    @PutMapping("/users/{targetUserId}/role")
    public ApiResponse<AdminUserRowDto> changeUserRole(@PathVariable Long targetUserId,
                                                       @Valid @RequestBody ChangeUserRoleRequest request) {
        return ApiResponse.ok("Cập nhật phân quyền tài khoản thành công!", systemAdminService.changeUserRole(targetUserId, request));
    }

    // --- ITEM, BANNER & GACHA DYNAMIC CONFIG (#105, #106, #107) ---
    @Operation(summary = "[#106A] Thêm mới Vật phẩm vào danh mục (Gắn icon 2D tĩnh, setup chỉ số Buff)")
    @PostMapping("/items")
    public ApiResponse<ItemDetailDto> createItem(@Valid @RequestBody UpsertItemRequest request) {
        return ApiResponse.ok("Thêm mới vật phẩm vào hệ thống thành công!", systemAdminService.createCatalogItem(request));
    }

    @Operation(summary = "[#106B] Chỉnh sửa thông số Buff / Giá bán / Ảnh 2D tĩnh của Vật phẩm")
    @PutMapping("/items/{itemId}")
    public ApiResponse<ItemDetailDto> updateItem(@PathVariable Integer itemId,
                                                 @RequestBody UpsertItemRequest request) {
        return ApiResponse.ok("Cập nhật vật phẩm thành công!", systemAdminService.updateCatalogItem(itemId, request));
    }

    @Operation(summary = "[#107A] Tạo mới Banner Hộp mù (Gắn ảnh tĩnh, Giá mở, Mốc bảo hiểm Pity)")
    @PostMapping("/gacha/banners")
    public ApiResponse<NxvGachaBanners> createBanner(@Valid @RequestBody UpsertGachaBannerRequest request) {
        return ApiResponse.ok("Tạo mới Banner Hộp mù thành công!", systemAdminService.createGachaBanner(request));
    }

    @Operation(summary = "[#107B, #105] Cập nhật Banner Hộp mù (Đổi giá quay, chỉnh sửa mốc Pity, đóng/mở Banner)")
    @PutMapping("/gacha/banners/{bannerId}")
    public ApiResponse<NxvGachaBanners> updateBanner(@PathVariable Integer bannerId,
                                                     @RequestBody UpsertGachaBannerRequest request) {
        return ApiResponse.ok("Cập nhật Banner Hộp mù thành công!", systemAdminService.updateGachaBanner(bannerId, request));
    }

    @Operation(summary = "[#105] Cấu hình động Tỷ lệ rớt đồ (Drop Rate) và Vật phẩm Featured cho Banner Gacha")
    @PostMapping("/gacha/drop-rates")
    public ApiResponse<Map<String, Object>> upsertDropRate(@Valid @RequestBody UpsertDropRateRequest request) {
        return ApiResponse.ok("Cập nhật tỷ lệ rớt đồ Gacha thành công!", systemAdminService.upsertGachaDropRate(request));
    }

    // --- DIFFICULTY PENALTY & CRONJOB MONITOR (#108, #109) ---
    @Operation(summary = "[#108A] Xem cấu hình hệ số Phạt HP và Thưởng mặc định cho từng độ khó của Task")
    @GetMapping("/config/difficulty-penalties")
    public ApiResponse<DifficultyPenaltyConfigDto> getDifficultyConfig() {
        return ApiResponse.ok(systemAdminService.getDifficultyPenaltyConfig());
    }

    @Operation(summary = "[#108B] Cập nhật hệ số Phạt (Lượng HP bị trừ) mặc định cho từng độ khó của Task")
    @PutMapping("/config/difficulty-penalties")
    public ApiResponse<Map<String, Object>> updateDifficultyConfig(
            @RequestBody DifficultyPenaltyConfigDto request,
            @RequestParam(defaultValue = "false") boolean applyToExistingPendingTasks) {
        return ApiResponse.ok("Đã cập nhật hệ số phạt độ khó!", systemAdminService.updateDifficultyPenaltyConfig(request, applyToExistingPendingTasks));
    }

    @Operation(summary = "[#109] Theo dõi và kích hoạt chạy kiểm tra toàn bộ các Cronjob ngầm (Trừ máu Task quá hạn & Pet ngủ đông)")
    @PostMapping("/cronjobs/run-all")
    public ApiResponse<Map<String, Object>> runAllCronjobs() {
        return ApiResponse.ok("Đã thực thi toàn bộ Cronjob hệ thống!", systemAdminService.triggerAllSystemCronjobsManually());
    }

    // --- GLOBAL NOTIFICATIONS (#110) ---
    @Operation(summary = "[#110A] Gửi thông báo (Global Notification đẩy đến toàn bộ Client nếu targetUserId = null)")
    @PostMapping("/notifications/send")
    public ApiResponse<NotificationDto> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        return ApiResponse.ok("Đã phát thông báo thành công!", systemAdminService.sendNotification(request));
    }

    @Operation(summary = "[#110B] Xem danh sách thông báo của tôi và Global Notifications")
    @GetMapping("/notifications/me")
    public ApiResponse<List<NotificationDto>> getMyNotifications() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(systemAdminService.getMyAndGlobalNotifications(userId));
    }

    @Operation(summary = "[#110C] Đánh dấu đã đọc thông báo")
    @PatchMapping("/notifications/{notificationId}/read")
    public ApiResponse<NotificationDto> markRead(@PathVariable Long notificationId) {
        return ApiResponse.ok(systemAdminService.markNotificationRead(notificationId));
    }

    // --- SYSTEM LOGS, TAGS, REPORTS & AI CONFIG (#111, #112, #114, #115) ---
    @Operation(summary = "[#111] Xem Log hệ thống (Lịch sử nạp/trừ Vàng, EXP, HP toàn Server để phát hiện bug)")
    @GetMapping("/logs/stat-logs")
    public ApiResponse<List<Map<String, Object>>> getGlobalStatLogs(@RequestParam(required = false) String logType) {
        return ApiResponse.ok(systemAdminService.getGlobalServerStatLogs(logType));
    }

    @Operation(summary = "[#121-ADMIN] Đối soát toàn bộ Hóa đơn tiền thật (Billing) & Lịch sử Webhook Ngân hàng")
    @GetMapping("/logs/billing-webhooks")
    public ApiResponse<Map<String, Object>> getBillingAndWebhookLogs() {
        return ApiResponse.ok(systemAdminService.getAllBillingAndWebhookLogsForAdmin());
    }

    @Operation(summary = "[#112A] Cập nhật Thẻ chuyên môn (Tag) mặc định của hệ thống")
    @PutMapping("/tags/{tagId}")
    public ApiResponse<NxvTags> updateTag(@PathVariable Integer tagId,
                                          @RequestParam String tagName,
                                          @RequestParam(required = false) String colorHex) {
        return ApiResponse.ok("Cập nhật Tag thành công!", systemAdminService.updateSystemTag(tagId, tagName, colorHex));
    }

    @Operation(summary = "[#112B] Xóa Thẻ chuyên môn (Tag)")
    @DeleteMapping("/tags/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Integer tagId) {
        systemAdminService.deleteSystemTag(tagId);
        return ApiResponse.ok("Đã xóa Tag thành công!", null);
    }

    @Operation(summary = "[#114A] Người chơi gửi Khiếu nại / Báo cáo lỗi (Report)")
    @PostMapping("/reports")
    public ApiResponse<UserReportDto> submitReport(@Valid @RequestBody CreateUserReportRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã gửi báo cáo lên Ban quản trị!", systemAdminService.submitUserReport(userId, request));
    }

    @Operation(summary = "[#114B] Admin xem danh sách Khiếu nại / Report từ người dùng")
    @GetMapping("/reports")
    public ApiResponse<List<UserReportDto>> getAllReports(@RequestParam(required = false) Integer status) {
        return ApiResponse.ok(systemAdminService.getAllUserReports(status));
    }

    @Operation(summary = "[#114C] Admin duyệt và xử lý Khiếu nại / Report (1 = Resolved, 2 = Rejected)")
    @PutMapping("/reports/{reportId}/resolve")
    public ApiResponse<UserReportDto> resolveReport(@PathVariable Long reportId,
                                                    @Valid @RequestBody ResolveReportRequest request) {
        return ApiResponse.ok("Đã xử lý báo cáo khiếu nại!", systemAdminService.resolveUserReport(reportId, request));
    }

    @Operation(summary = "[#115A] Xem cấu hình API kết nối AI Chatbot hiện tại")
    @GetMapping("/ai-config")
    public ApiResponse<List<NxvAIConfig>> getAiConfigs() {
        return ApiResponse.ok(systemAdminService.getAllAiConfigs());
    }

    @Operation(summary = "[#115B] Cập nhật cấu hình API kết nối AI Chatbot (Đổi Token / Model / System Prompt)")
    @PutMapping("/ai-config")
    public ApiResponse<NxvAIConfig> upsertAiConfig(@Valid @RequestBody UpsertAiConfigRequest request) {
        return ApiResponse.ok("Cập nhật cấu hình AI Chatbot thành công!", systemAdminService.upsertAiConfig(request));
    }
}