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
@Tag(name = "Module 6B: Há»™p mÃ¹ Gacha, Pity 50 láº§n & Chá»£ Marketplace (Chá»©c nÄƒng #72 - #77, #85 - #88)", description = "Banner Gacha, Má»Ÿ há»™p mÃ¹ x1/x10, Báº£o hiá»ƒm Pity 50 láº§n, Lá»‹ch sá»­ Gacha & SÃ n giao dá»‹ch Marketplace thuáº¿ 5%")
public class NxvGachaMarketController {

    private final NxvGachaMarketService gachaMarketService;

    // --- HE THONG HOP MU (GACHA & PITY SYSTEM) (#72 -> #77) ---
    @Operation(summary = "[#72, #76, #77] Xem danh sÃ¡ch cÃ¡c Banner/Há»™p mÃ¹ Ä‘ang má»Ÿ bÃ¡n, Báº£ng tá»· lá»‡ rá»›t Ä‘á»“ (Drop Rate) vÃ  Bá»™ Ä‘áº¿m Pity hiá»‡n táº¡i")
    @GetMapping("/banners")
    public ApiResponse<List<GachaBannerDto>> getActiveBanners() {
        Long userId = null;
        try {
            userId = NxvSecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {}
        return ApiResponse.ok(gachaMarketService.getActiveBanners(userId));
    }

    @Operation(summary = "[#73, #76, #77] Mua / Má»Ÿ Há»™p mÃ¹ 1 láº§n (x1 - Trá»« VÃ ng, tÃ­ch lÅ©y Pity 50 láº§n)")
    @PostMapping("/banners/{bannerId}/pull-x1")
    public ApiResponse<GachaPullResponse> pullOne(@PathVariable Integer bannerId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Má»Ÿ Há»™p mÃ¹ x1 thÃ nh cÃ´ng!", gachaMarketService.pullGacha(userId, bannerId, 1));
    }

    @Operation(summary = "[#74, #76, #77] Mua / Má»Ÿ Há»™p mÃ¹ 10 láº§n liÃªn tiáº¿p (x10 - Trá»« VÃ ng, tÃ­ch lÅ©y & kÃ­ch hoáº¡t báº£o hiá»ƒm Pity)")
    @PostMapping("/banners/{bannerId}/pull-x10")
    public ApiResponse<GachaPullResponse> pullTen(@PathVariable Integer bannerId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Má»Ÿ Há»™p mÃ¹ x10 thÃ nh cÃ´ng!", gachaMarketService.pullGacha(userId, bannerId, 10));
    }

    @Operation(summary = "[#75A] Xem lá»‹ch sá»­ Má»Ÿ há»™p mÃ¹ cá»§a báº£n thÃ¢n")
    @GetMapping("/gacha/history/me")
    public ApiResponse<List<GachaHistoryDto>> getMyGachaHistory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(gachaMarketService.getGachaHistory(userId, false));
    }

    @Operation(summary = "[#75B] Xem lá»‹ch sá»­ Má»Ÿ há»™p mÃ¹ toÃ n Server (Ai quay ra Ä‘á»“ gÃ¬, vÃ o lÃºc nÃ o)")
    @GetMapping("/gacha/history/global")
    public ApiResponse<List<GachaHistoryDto>> getGlobalGachaHistory() {
        return ApiResponse.ok(gachaMarketService.getGachaHistory(null, true));
    }

    // --- THI TRUONG GIAO DICH (MARKETPLACE & 5% TAX) (#85 -> #88) ---
    @Operation(summary = "[#85A] Xem danh sÃ¡ch váº­t pháº©m Ä‘ang rao bÃ¡n trÃªn Chá»£ giao dá»‹ch (Marketplace)")
    @GetMapping("/marketplace/listings")
    public ApiResponse<List<MarketListingDto>> getActiveListings(
            @RequestParam(required = false) Integer itemType,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(gachaMarketService.getActiveMarketListings(itemType, keyword));
    }

    @Operation(summary = "[#85B] Xem danh sÃ¡ch cÃ¡c máº·t hÃ ng do chÃ­nh tÃ´i Ä‘Äƒng bÃ¡n")
    @GetMapping("/marketplace/listings/me")
    public ApiResponse<List<MarketListingDto>> getMyListings() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(gachaMarketService.getMyMarketListings(userId));
    }

    @Operation(summary = "[#85C] ÄÄƒng bÃ¡n váº­t pháº©m áº£o lÃªn Chá»£ giao dá»‹ch (Marketplace)")
    @PostMapping("/marketplace/listings")
    public ApiResponse<MarketListingDto> createListing(@Valid @RequestBody CreateMarketListingRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÄƒng bÃ¡n váº­t pháº©m lÃªn Marketplace thÃ nh cÃ´ng!", gachaMarketService.createMarketListing(userId, request));
    }

    @Operation(summary = "[#85D] Há»§y / Gá»¡ bÃ i Ä‘Äƒng bÃ¡n trÃªn Chá»£ vÃ  nháº­n láº¡i váº­t pháº©m vá» TÃºi Ä‘á»“")
    @PostMapping("/marketplace/listings/{listingId}/cancel")
    public ApiResponse<MarketListingDto> cancelListing(@PathVariable Long listingId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ gá»¡ váº­t pháº©m khá»i chá»£ vÃ  hoÃ n vá» tÃºi Ä‘á»“!", gachaMarketService.cancelMarketListing(userId, listingId));
    }

    @Operation(summary = "[#86, #88] Mua váº­t pháº©m tá»« ngÆ°á»i chÆ¡i khÃ¡c báº±ng Gold (Há»‡ thá»‘ng tá»± Ä‘á»™ng kháº¥u trá»« 5% thuáº¿ giao dá»‹ch vá» Server)")
    @PostMapping("/marketplace/listings/{listingId}/buy")
    public ApiResponse<MarketTransactionDto> buyListing(@PathVariable Long listingId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mua váº­t pháº©m trÃªn Marketplace thÃ nh cÃ´ng!", gachaMarketService.buyMarketListing(userId, listingId));
    }

    @Operation(summary = "[#87] Xem lá»‹ch sá»­ giao dá»‹ch trÃªn Marketplace (mode = ALL / BOUGHT / SOLD)")
    @GetMapping("/marketplace/transactions")
    public ApiResponse<List<MarketTransactionDto>> getMarketTransactions(@RequestParam(defaultValue = "ALL") String mode) {
        Long userId = null;
        try {
            userId = NxvSecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {}
        return ApiResponse.ok(gachaMarketService.getMarketTransactions(userId, mode));
    }
}