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
@Tag(name = "Module 6A: Túi đồ, Trang bị & Vật phẩm (Chức năng #78 - #84, #89, #90)", description = "Quản lý Túi đồ, Mặc/Tháo đồ qua Stored Procedure, Uống bình máu, Mở rộng ô chứa, Bán & Tặng vật phẩm")
public class NxvInventoryController {

    private final NxvInventoryService inventoryService;

    @Operation(summary = "[#78, #82] Xem toàn bộ Túi đồ, các ô Trang bị đang mặc và Tổng chỉ số Buff cộng thêm")
    @GetMapping("/me")
    public ApiResponse<InventoryOverviewDto> getMyInventory() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(inventoryService.getMyInventoryOverview(userId));
    }

    @Operation(summary = "[#79A] Xem danh mục tất cả Vật phẩm trong hệ thống (Lọc theo itemType nếu cần)")
    @GetMapping("/catalog")
    public ApiResponse<List<ItemDetailDto>> getCatalogItems(@RequestParam(required = false) Integer itemType) {
        return ApiResponse.ok(inventoryService.getAllCatalogItems(itemType));
    }

    @Operation(summary = "[#79B] Xem chi tiết 1 Vật phẩm (Icon 2D tĩnh, các dòng chỉ số Buff, Giá bán)")
    @GetMapping("/catalog/{itemId}")
    public ApiResponse<ItemDetailDto> getItemDetail(@PathVariable Integer itemId) {
        return ApiResponse.ok(inventoryService.getItemDetail(itemId));
    }

    @Operation(summary = "[#80, #82] Mặc (Equip) trang bị lên người (Gọi NxvSp_EquipItem tính lại MaxHP/MaxMP, không đổi ảnh Avatar)")
    @PostMapping("/equip")
    public ApiResponse<InventoryOverviewDto> equipItem(@Valid @RequestBody EquipItemRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mặc trang bị và cập nhật chỉ số Buff thành công!", inventoryService.equipItem(userId, request));
    }

    @Operation(summary = "[#81, #82] Tháo (Unequip) trang bị khỏi Slot (Gọi NxvSp_UnequipItem tính lại MaxHP/MaxMP)")
    @PostMapping("/unequip")
    public ApiResponse<InventoryOverviewDto> unequipItem(@Valid @RequestBody UnequipSlotRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Tháo trang bị thành công!", inventoryService.unequipItem(userId, request));
    }

    @Operation(summary = "[#89] Sử dụng vật phẩm tiêu hao - Uống bình máu hồi HP (Gọi NxvSp_UseConsumable)")
    @PostMapping("/use-consumable")
    public ApiResponse<InventoryOverviewDto> useConsumable(@Valid @RequestBody UseConsumableRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Sử dụng vật phẩm hồi phục thành công!", inventoryService.useConsumable(userId, request));
    }

    @Operation(summary = "[#83] Mở rộng thêm ô trống cho Túi đồ bằng Vàng (+10 ô / 200 Gold)")
    @PostMapping("/expand")
    public ApiResponse<InventoryOverviewDto> expandInventory(@Valid @RequestBody ExpandInventoryRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Mở rộng túi đồ thành công!", inventoryService.expandInventorySlots(userId, request));
    }

    @Operation(summary = "[#84] Bán vật phẩm thừa/rác để thu hồi Vàng")
    @PostMapping("/sell")
    public ApiResponse<Map<String, Object>> sellItem(@Valid @RequestBody SellItemRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Bán vật phẩm thu hồi Vàng thành công!", inventoryService.sellItem(userId, request));
    }

    @Operation(summary = "[#90] Gửi tặng vật phẩm tĩnh cho người chơi khác")
    @PostMapping("/gift")
    public ApiResponse<Map<String, Object>> giftItem(@Valid @RequestBody GiftItemRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Gửi tặng vật phẩm cho người chơi thành công!", inventoryService.giftItemToPlayer(userId, request));
    }
}