package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvInventoryDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvInventoryService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Module 6A: TÃºi Ä‘á»“, Trang bá»‹ & Váº­t pháº©m (Chá»©c nÄƒng #78 - #84, #89, #90)", description = "Quáº£n lÃ½ TÃºi Ä‘á»“, Máº·c/ThÃ¡o Ä‘á»“ qua Stored Procedure, Uá»‘ng bÃ¬nh mÃ¡u, Má»Ÿ rá»™ng Ã´ chá»©a, BÃ¡n & Táº·ng váº­t pháº©m")
public class NxvInventoryController {

    private final NxvInventoryService inventoryService;

    @Operation(summary = "[#78, #82] Xem toÃ n bá»™ TÃºi Ä‘á»“, cÃ¡c Ã´ Trang bá»‹ Ä‘ang máº·c vÃ  Tá»•ng chá»‰ sá»‘ Buff cá»™ng thÃªm")
    @GetMapping("/me")
    public ApiResponse<InventoryOverviewDto> getMyInventory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(inventoryService.getMyInventoryOverview(userId));
    }

    @Operation(summary = "[#79A] Xem danh má»¥c táº¥t cáº£ Váº­t pháº©m trong há»‡ thá»‘ng (Lá»c theo itemType náº¿u cáº§n)")
    @GetMapping("/catalog")
    public ApiResponse<List<ItemDetailDto>> getCatalogItems(@RequestParam(required = false) Integer itemType) {
        return ApiResponse.ok(inventoryService.getAllCatalogItems(itemType));
    }

    @Operation(summary = "[#79B] Xem chi tiáº¿t 1 Váº­t pháº©m (Icon 2D tÄ©nh, cÃ¡c dÃ²ng chá»‰ sá»‘ Buff, GiÃ¡ bÃ¡n)")
    @GetMapping("/catalog/{itemId}")
    public ApiResponse<ItemDetailDto> getItemDetail(@PathVariable Integer itemId) {
        return ApiResponse.ok(inventoryService.getItemDetail(itemId));
    }

    @Operation(summary = "[#80, #82] Máº·c (Equip) trang bá»‹ lÃªn ngÆ°á»i (Gá»i NxvSp_EquipItem tÃ­nh láº¡i MaxHP/MaxMP, khÃ´ng Ä‘á»•i áº£nh Avatar)")
    @PostMapping("/equip")
    public ApiResponse<InventoryOverviewDto> equipItem(@Valid @RequestBody EquipItemRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Máº·c trang bá»‹ vÃ  cáº­p nháº­t chá»‰ sá»‘ Buff thÃ nh cÃ´ng!", inventoryService.equipItem(userId, request));
    }

    @Operation(summary = "[#81, #82] ThÃ¡o (Unequip) trang bá»‹ khá»i Slot (Gá»i NxvSp_UnequipItem tÃ­nh láº¡i MaxHP/MaxMP)")
    @PostMapping("/unequip")
    public ApiResponse<InventoryOverviewDto> unequipItem(@Valid @RequestBody UnequipSlotRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ThÃ¡o trang bá»‹ thÃ nh cÃ´ng!", inventoryService.unequipItem(userId, request));
    }

    @Operation(summary = "[#89] Sá»­ dá»¥ng váº­t pháº©m tiÃªu hao - Uá»‘ng bÃ¬nh mÃ¡u há»“i HP (Gá»i NxvSp_UseConsumable)")
    @PostMapping("/use-consumable")
    public ApiResponse<InventoryOverviewDto> useConsumable(@Valid @RequestBody UseConsumableRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Sá»­ dá»¥ng váº­t pháº©m há»“i phá»¥c thÃ nh cÃ´ng!", inventoryService.useConsumable(userId, request));
    }

    @Operation(summary = "[#83] Má»Ÿ rá»™ng thÃªm Ã´ trá»‘ng cho TÃºi Ä‘á»“ báº±ng VÃ ng (+10 Ã´ / 200 Gold)")
    @PostMapping("/expand")
    public ApiResponse<InventoryOverviewDto> expandInventory(@Valid @RequestBody ExpandInventoryRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Má»Ÿ rá»™ng tÃºi Ä‘á»“ thÃ nh cÃ´ng!", inventoryService.expandInventorySlots(userId, request));
    }

    @Operation(summary = "[#84] BÃ¡n váº­t pháº©m thá»«a/rÃ¡c Ä‘á»ƒ thu há»“i VÃ ng")
    @PostMapping("/sell")
    public ApiResponse<Map<String, Object>> sellItem(@Valid @RequestBody SellItemRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("BÃ¡n váº­t pháº©m thu há»“i VÃ ng thÃ nh cÃ´ng!", inventoryService.sellItem(userId, request));
    }

    @Operation(summary = "[#90] Gá»­i táº·ng váº­t pháº©m tÄ©nh cho ngÆ°á»i chÆ¡i khÃ¡c")
    @PostMapping("/gift")
    public ApiResponse<Map<String, Object>> giftItem(@Valid @RequestBody GiftItemRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Gá»­i táº·ng váº­t pháº©m cho ngÆ°á»i chÆ¡i thÃ nh cÃ´ng!", inventoryService.giftItemToPlayer(userId, request));
    }
}