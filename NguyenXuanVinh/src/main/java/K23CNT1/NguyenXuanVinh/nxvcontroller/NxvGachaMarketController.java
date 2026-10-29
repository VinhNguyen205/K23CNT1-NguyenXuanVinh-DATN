package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvGachaMarketDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvGachaMarketService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gacha-market")
@RequiredArgsConstructor
@Tag(name = "Module 6B: Hộp mù Gacha, Pity 50 lần & Chợ Marketplace (Chức năng #72 - #77, #85 - #88)", description = "Banner Gacha, Mở hộp mù x1/x10, Bảo hiểm Pity 50 lần, Lịch sử Gacha & Sàn giao dịch Marketplace thuế 5%")
public class NxvGachaMarketController {

    private final NxvGachaMarketService gachaMarketService;

    // --- HE THONG HOP MU (GACHA & PITY SYSTEM) (#72 -> #77) ---
    @Operation(summary = "[#72, #76, #77] Xem danh sách các Banner/Hộp mù đang mở bán, Bảng tỷ lệ rớt đồ (Drop Rate) và Bộ đếm Pity hiện tại")
    @GetMapping("/banners")
    public ApiResponse<List<GachaBannerDto>> getActiveBanners() {
        Long userId = null;
        try {
            userId = NxvSecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {}
        return ApiResponse.ok(gachaMarketService.getActiveBanners(userId));
    }

    @Operation(summary = "[#73, #76, #77] Mua / Mở Hộp mù 1 lần (x1 - Trừ Vàng, tích lũy Pity 50 lần)")
    @PostMapping("/banners/{bannerId}/pull-x1")
    public ApiResponse<GachaPullResponse> pullOne(@PathVariable Integer bannerId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mở Hộp mù x1 thành công!", gachaMarketService.pullGacha(userId, bannerId, 1));
    }

    @Operation(summary = "[#74, #76, #77] Mua / Mở Hộp mù 10 lần liên tiếp (x10 - Trừ Vàng, tích lũy & kích hoạt bảo hiểm Pity)")
    @PostMapping("/banners/{bannerId}/pull-x10")
    public ApiResponse<GachaPullResponse> pullTen(@PathVariable Integer bannerId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mở Hộp mù x10 thành công!", gachaMarketService.pullGacha(userId, bannerId, 10));
    }

    @Operation(summary = "[#75A] Xem lịch sử Mở hộp mù của bản thân")
    @GetMapping("/gacha/history/me")
    public ApiResponse<List<GachaHistoryDto>> getMyGachaHistory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(gachaMarketService.getGachaHistory(userId, false));
    }

    @Operation(summary = "[#75B] Xem lịch sử Mở hộp mù toàn Server (Ai quay ra đồ gì, vào lúc nào)")
    @GetMapping("/gacha/history/global")
    public ApiResponse<List<GachaHistoryDto>> getGlobalGachaHistory() {
        return ApiResponse.ok(gachaMarketService.getGachaHistory(null, true));
    }

    // --- THI TRUONG GIAO DICH (MARKETPLACE & 5% TAX) (#85 -> #88) ---
    @Operation(summary = "[#85A] Xem danh sách vật phẩm đang rao bán trên Chợ giao dịch (Marketplace)")
    @GetMapping("/marketplace/listings")
    public ApiResponse<List<MarketListingDto>> getActiveListings(
            @RequestParam(required = false) Integer itemType,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(gachaMarketService.getActiveMarketListings(itemType, keyword));
    }

    @Operation(summary = "[#85B] Xem danh sách các mặt hàng do chính tôi đăng bán")
    @GetMapping("/marketplace/listings/me")
    public ApiResponse<List<MarketListingDto>> getMyListings() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(gachaMarketService.getMyMarketListings(userId));
    }

    @Operation(summary = "[#85C] Đăng bán vật phẩm ảo lên Chợ giao dịch (Marketplace)")
    @PostMapping("/marketplace/listings")
    public ApiResponse<MarketListingDto> createListing(@Valid @RequestBody CreateMarketListingRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đăng bán vật phẩm lên Marketplace thành công!", gachaMarketService.createMarketListing(userId, request));
    }

    @Operation(summary = "[#85D] Hủy / Gỡ bài đăng bán trên Chợ và nhận lại vật phẩm về Túi đồ")
    @PostMapping("/marketplace/listings/{listingId}/cancel")
    public ApiResponse<MarketListingDto> cancelListing(@PathVariable Long listingId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã gỡ vật phẩm khỏi chợ và hoàn về túi đồ!", gachaMarketService.cancelMarketListing(userId, listingId));
    }

    @Operation(summary = "[#86, #88] Mua vật phẩm từ người chơi khác bằng Gold (Hệ thống tự động khấu trừ 5% thuế giao dịch về Server)")
    @PostMapping("/marketplace/listings/{listingId}/buy")
    public ApiResponse<MarketTransactionDto> buyListing(@PathVariable Long listingId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mua vật phẩm trên Marketplace thành công!", gachaMarketService.buyMarketListing(userId, listingId));
    }

    @Operation(summary = "[#87] Xem lịch sử giao dịch trên Marketplace (mode = ALL / BOUGHT / SOLD)")
    @GetMapping("/marketplace/transactions")
    public ApiResponse<List<MarketTransactionDto>> getMarketTransactions(@RequestParam(defaultValue = "ALL") String mode) {
        Long userId = null;
        try {
            userId = NxvSecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {}
        return ApiResponse.ok(gachaMarketService.getMarketTransactions(userId, mode));
    }
}