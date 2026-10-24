# ==============================================================================
# TASKTALE / LIFERPG - TASK 3 AUTOMATED GENERATOR (MODULE 6A: INVENTORY & EQUIP)
# Hoan thien Chuc nang #78 -> #84, #89, #90: Inventory, Item Details, Equip/Unequip
# (NxvSp_EquipItem / NxvSp_UnequipItem), Use Consumable (NxvSp_UseConsumable),
# Expand Inventory Slots, Sell Junk Items & Gift Items to Other Players
# ==============================================================================

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Write-JavaFile {
    param([string]$FilePath, [string]$Content)
    $dir = Split-Path -Parent $FilePath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    if (Test-Path $FilePath) {
        Remove-Item $FilePath -Force
    }
    [System.IO.File]::WriteAllText($FilePath, $Content, $utf8NoBom)
}

$javaRoot = Join-Path (Get-Location) "src\main\java"
$entityDirObj = Get-ChildItem -Path $javaRoot -Recurse -Directory -Filter "nxventity" | Select-Object -First 1
$baseDir = $entityDirObj.Parent.FullName
$basePkg = $baseDir.Substring($javaRoot.Length).TrimStart('\', '/') -replace '[\\/]', '.'

Write-Host "==> Dang sinh Module 6A (Inventory, Equipment, Consumables, Gift) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 6A
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvInventoryRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvInventoryRepository extends JpaRepository<NxvInventory, Long> {
    List<NxvInventory> findByUser_UserId(Long userId);
    List<NxvInventory> findByUser_UserIdAndQuantityGreaterThanOrderByInventoryIdDesc(Long userId, Integer minQuantity);
    Optional<NxvInventory> findByUser_UserIdAndItem_ItemId(Long userId, Integer itemId);
    long countByUser_UserIdAndQuantityGreaterThan(Long userId, Integer minQuantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_EquipItem @UserID = :userId, @ItemID = :itemId", nativeQuery = true)
    void executeEquipItemSp(@Param("userId") Long userId, @Param("itemId") Integer itemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_UnequipItem @UserID = :userId, @SlotID = :slotId", nativeQuery = true)
    void executeUnequipItemSp(@Param("userId") Long userId, @Param("slotId") Integer slotId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_UseConsumable @UserID = :userId, @ItemID = :itemId", nativeQuery = true)
    void executeUseConsumableSp(@Param("userId") Long userId, @Param("itemId") Integer itemId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvEquippedItemsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvEquippedItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvEquippedItemsRepository extends JpaRepository<NxvEquippedItems, Long> {
    List<NxvEquippedItems> findByUser_UserId(Long userId);
    Optional<NxvEquippedItems> findByUser_UserIdAndSlot_SlotId(Long userId, Integer slotId);
    boolean existsByUser_UserIdAndItem_ItemId(Long userId, Integer itemId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvItemsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvItemsRepository extends JpaRepository<NxvItems, Integer> {
    List<NxvItems> findByItemTypeOrderByItemIdAsc(Integer itemType);
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 6A (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvInventoryDtos.java" @"
package $basePkg.nxvdto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class NxvInventoryDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemDetailDto {
        private Integer itemId;
        private String itemName;
        private Integer itemType;
        private String itemTypeLabel;
        private String imageUrl;
        private Integer equipSlotId;
        private String equipSlotName;
        private String equipSlotKey;
        private Integer buffMaxHp;
        private Integer buffMaxMp;
        private BigDecimal buffExpBonus;
        private BigDecimal buffGoldBonus;
        private BigDecimal buffDeadlineDmgReduce;
        private Integer healHp;
        private Integer sellPrice;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryItemDto {
        private Long inventoryId;
        private Integer quantity;
        private Boolean isEquipped;
        private Integer equippedSlotId;
        private ItemDetailDto item;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EquippedSlotDto {
        private Integer slotId;
        private String slotName;
        private String slotKey;
        private ItemDetailDto equippedItem;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryOverviewDto {
        private Long userId;
        private Integer usedSlots;
        private Integer maxSlots;
        private Integer expandCostGold;
        private Integer expandSlotsPerPurchase;
        private Long currentGold;
        private Integer baseMaxHp;
        private Integer effectiveMaxHp;
        private Integer currentHp;
        private Integer baseMaxMp;
        private Integer effectiveMaxMp;
        private Integer currentMp;
        private Integer totalBuffMaxHp;
        private Integer totalBuffMaxMp;
        private BigDecimal totalExpBonus;
        private BigDecimal totalGoldBonus;
        private BigDecimal totalDmgReduce;
        private List<EquippedSlotDto> equippedSlots;
        private List<InventoryItemDto> items;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EquipItemRequest {
        @NotNull(message = "itemId không được để trống")
        private Integer itemId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UnequipSlotRequest {
        @NotNull(message = "slotId không được để trống")
        private Integer slotId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UseConsumableRequest {
        @NotNull(message = "itemId không được để trống")
        private Integer itemId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ExpandInventoryRequest {
        @Builder.Default
        @Min(value = 1, message = "Số lần mở rộng tối thiểu là 1")
        private Integer packs = 1; // Moi pack = +10 slots voi gia 200 Gold
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SellItemRequest {
        @NotNull(message = "itemId không được để trống")
        private Integer itemId;

        @NotNull(message = "Số lượng bán không được để trống")
        @Min(value = 1, message = "Số lượng bán tối thiểu là 1")
        private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GiftItemRequest {
        @NotNull(message = "ID người nhận (receiverUserId) không được để trống")
        private Long receiverUserId;

        @NotNull(message = "itemId không được để trống")
        private Integer itemId;

        @NotNull(message = "Số lượng tặng không được để trống")
        @Min(value = 1, message = "Số lượng tặng tối thiểu là 1")
        private Integer quantity;

        private String message;
    }
}
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 6A (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvInventoryService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvInventoryDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvenums.NxvConstants;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvInventoryService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvItemsRepository itemsRepository;
    private final NxvEquipmentSlotsRepository equipmentSlotsRepository;
    private final NxvUserInventoryConfigRepository inventoryConfigRepository;
    private final NxvInventoryRepository inventoryRepository;
    private final NxvEquippedItemsRepository equippedItemsRepository;
    private final NxvVwUserTotalBuffsRepository totalBuffsRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;

    public static final int EXPAND_SLOTS_PER_PACK = 10;
    public static final long EXPAND_COST_GOLD_PER_PACK = 200L;

    // Chuc nang #78 & #82: Xem toan bo Tui do, Trang bi dang mac & Tong chi so Buff
    @Transactional(readOnly = true)
    public InventoryOverviewDto getMyInventoryOverview(Long userId) {
        NxvCharacters c = getCharacter(userId);
        NxvUserInventoryConfig cfg = getOrCreateInventoryConfig(userId);
        List<NxvInventory> rawList = inventoryRepository
                .findByUser_UserIdAndQuantityGreaterThanOrderByInventoryIdDesc(userId, 0);

        Map<Integer, Integer> equippedItemToSlotMap = new HashMap<>();
        Map<Integer, NxvItems> slotToEquippedItemMap = new HashMap<>();
        for (NxvEquippedItems ei : equippedItemsRepository.findByUser_UserId(userId)) {
            equippedItemToSlotMap.put(ei.getItem().getItemId(), ei.getSlot().getSlotId());
            slotToEquippedItemMap.put(ei.getSlot().getSlotId(), ei.getItem());
        }

        List<EquippedSlotDto> slotDtos = equipmentSlotsRepository.findAll().stream()
                .map(slot -> EquippedSlotDto.builder()
                        .slotId(slot.getSlotId())
                        .slotName(slot.getSlotName())
                        .slotKey(slot.getSlotKey())
                        .equippedItem(slotToEquippedItemMap.containsKey(slot.getSlotId())
                                ? toItemDetailDto(slotToEquippedItemMap.get(slot.getSlotId()))
                                : null)
                        .build())
                .collect(Collectors.toList());

        List<InventoryItemDto> itemDtos = rawList.stream()
                .map(inv -> {
                    Integer itemId = inv.getItem().getItemId();
                    return InventoryItemDto.builder()
                            .inventoryId(inv.getInventoryId())
                            .quantity(inv.getQuantity())
                            .isEquipped(equippedItemToSlotMap.containsKey(itemId))
                            .equippedSlotId(equippedItemToSlotMap.get(itemId))
                            .item(toItemDetailDto(inv.getItem()))
                            .build();
                })
                .collect(Collectors.toList());

        NxvVwUserTotalBuffs buffs = totalBuffsRepository.findById(userId).orElse(null);

        return InventoryOverviewDto.builder()
                .userId(userId)
                .usedSlots(itemDtos.size())
                .maxSlots(cfg.getMaxSlots())
                .expandCostGold((int) EXPAND_COST_GOLD_PER_PACK)
                .expandSlotsPerPurchase(EXPAND_SLOTS_PER_PACK)
                .currentGold(c.getGold())
                .baseMaxHp(c.getBaseMaxHp())
                .effectiveMaxHp(c.getMaxHp())
                .currentHp(c.getCurrentHp())
                .baseMaxMp(c.getBaseMaxMp())
                .effectiveMaxMp(c.getMaxMp())
                .currentMp(c.getCurrentMp())
                .totalBuffMaxHp(buffs != null && buffs.getTotalBuffMaxHp() != null ? buffs.getTotalBuffMaxHp() : 0)
                .totalBuffMaxMp(buffs != null && buffs.getTotalBuffMaxMp() != null ? buffs.getTotalBuffMaxMp() : 0)
                .totalExpBonus(buffs != null && buffs.getTotalExpBonus() != null ? buffs.getTotalExpBonus() : BigDecimal.ZERO)
                .totalGoldBonus(buffs != null && buffs.getTotalGoldBonus() != null ? buffs.getTotalGoldBonus() : BigDecimal.ZERO)
                .totalDmgReduce(buffs != null && buffs.getTotalDmgReduce() != null ? buffs.getTotalDmgReduce() : BigDecimal.ZERO)
                .equippedSlots(slotDtos)
                .items(itemDtos)
                .build();
    }

    // Chuc nang #79: Xem danh muc & chi tiet Item (Hien thi icon 2D tinh va cac dong chi so)
    @Transactional(readOnly = true)
    public List<ItemDetailDto> getAllCatalogItems(Integer itemType) {
        List<NxvItems> list = (itemType != null)
                ? itemsRepository.findByItemTypeOrderByItemIdAsc(itemType)
                : itemsRepository.findAll();
        return list.stream().map(this::toItemDetailDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemDetailDto getItemDetail(Integer itemId) {
        NxvItems item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy vật phẩm ID: " + itemId));
        return toItemDetailDto(item);
    }

    // Chuc nang #80 & #82: Mac trang bi (Goi NxvSp_EquipItem -> Tinh lai MaxHP/MaxMP, KHONG doi Avatar)
    @Transactional
    public InventoryOverviewDto equipItem(Long userId, EquipItemRequest req) {
        assertUserAlive(userId);
        inventoryRepository.executeEquipItemSp(userId, req.getItemId());
        return getMyInventoryOverview(userId);
    }

    // Chuc nang #81 & #82: Thao trang bi (Goi NxvSp_UnequipItem -> Tinh lai MaxHP/MaxMP)
    @Transactional
    public InventoryOverviewDto unequipItem(Long userId, UnequipSlotRequest req) {
        assertUserAlive(userId);
        inventoryRepository.executeUnequipItemSp(userId, req.getSlotId());
        return getMyInventoryOverview(userId);
    }

    // Chuc nang #89: Su dung vat pham tieu hao (Goi NxvSp_UseConsumable -> Uong binh mau hoi HP)
    @Transactional
    public InventoryOverviewDto useConsumable(Long userId, UseConsumableRequest req) {
        assertUserAlive(userId);
        inventoryRepository.executeUseConsumableSp(userId, req.getItemId());
        return getMyInventoryOverview(userId);
    }

    // Chuc nang #83: Mo rong them o trong cho Tui do bang Vang
    @Transactional
    public InventoryOverviewDto expandInventorySlots(Long userId, ExpandInventoryRequest req) {
        assertUserAlive(userId);
        int packs = (req != null && req.getPacks() != null && req.getPacks() > 0) ? req.getPacks() : 1;
        long totalCost = packs * EXPAND_COST_GOLD_PER_PACK;
        int addedSlots = packs * EXPAND_SLOTS_PER_PACK;

        NxvCharacters c = getCharacter(userId);
        if (c.getGold() < totalCost) {
            throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                    "Không đủ Vàng! Cần " + totalCost + " Gold để mở rộng thêm " + addedSlots + " ô túi đồ.");
        }

        c.setGold(c.getGold() - totalCost);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        NxvUserInventoryConfig cfg = getOrCreateInventoryConfig(userId);
        cfg.setMaxSlots(cfg.getMaxSlots() + addedSlots);
        inventoryConfigRepository.save(cfg);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(c.getUser())
                .logType("Gold")
                .delta(-totalCost)
                .sourceType("ExpandInventory")
                .createdAt(LocalDateTime.now())
                .build());

        return getMyInventoryOverview(userId);
    }

    // Chuc nang #84: Ban vat pham thua/rac de thu hoi Vang
    @Transactional
    public Map<String, Object> sellItem(Long userId, SellItemRequest req) {
        assertUserAlive(userId);
        NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(userId, req.getItemId())
                .filter(i -> i.getQuantity() >= req.getQuantity())
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY, "Số lượng vật phẩm trong túi đồ không đủ để bán!"));

        boolean isEquipped = equippedItemsRepository.existsByUser_UserIdAndItem_ItemId(userId, req.getItemId());
        int remaining = inv.getQuantity() - req.getQuantity();
        if (isEquipped && remaining < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Vật phẩm này đang được mặc trên người! Vui lòng tháo trang bị trước khi bán chiếc cuối cùng.");
        }

        int unitPrice = (inv.getItem().getSellPrice() != null && inv.getItem().getSellPrice() > 0)
                ? inv.getItem().getSellPrice() : 10;
        long goldEarned = (long) unitPrice * req.getQuantity();

        inv.setQuantity(remaining);
        inventoryRepository.save(inv);

        NxvCharacters c = getCharacter(userId);
        c.setGold(c.getGold() + goldEarned);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(c.getUser())
                .logType("Gold")
                .delta(goldEarned)
                .sourceType("SellItem")
                .sourceId(inv.getItem().getItemId().longValue())
                .createdAt(LocalDateTime.now())
                .build());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("soldItemId", inv.getItem().getItemId());
        res.put("soldItemName", inv.getItem().getItemName());
        res.put("quantitySold", req.getQuantity());
        res.put("goldEarned", goldEarned);
        res.put("newGoldBalance", c.getGold());
        return res;
    }

    // Chuc nang #90: Gui tang vat pham tinh cho nguoi choi khac
    @Transactional
    public Map<String, Object> giftItemToPlayer(Long senderUserId, GiftItemRequest req) {
        assertUserAlive(senderUserId);
        if (Objects.equals(senderUserId, req.getReceiverUserId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể tự gửi tặng vật phẩm cho chính mình!");
        }

        Users sender = usersRepository.findById(senderUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người gửi!"));
        Users receiver = usersRepository.findById(req.getReceiverUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người chơi nhận quà!"));

        NxvInventory senderInv = inventoryRepository.findByUser_UserIdAndItem_ItemId(senderUserId, req.getItemId())
                .filter(i -> i.getQuantity() >= req.getQuantity())
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY, "Bạn không có đủ số lượng vật phẩm này để tặng!"));

        boolean isEquipped = equippedItemsRepository.existsByUser_UserIdAndItem_ItemId(senderUserId, req.getItemId());
        int senderRemaining = senderInv.getQuantity() - req.getQuantity();
        if (isEquipped && senderRemaining < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Vật phẩm này đang được trang bị! Vui lòng tháo ra trước khi tặng chiếc cuối cùng.");
        }

        // Kiem tra tui do cua nguoi nhan
        Optional<NxvInventory> receiverInvOpt = inventoryRepository.findByUser_UserIdAndItem_ItemId(receiver.getUserId(), req.getItemId());
        if (receiverInvOpt.isEmpty() || receiverInvOpt.get().getQuantity() == 0) {
            NxvUserInventoryConfig receiverCfg = getOrCreateInventoryConfig(receiver.getUserId());
            long receiverUsedSlots = inventoryRepository.countByUser_UserIdAndQuantityGreaterThan(receiver.getUserId(), 0);
            if (receiverUsedSlots >= receiverCfg.getMaxSlots()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Túi đồ của người nhận đã đầy (" + receiverCfg.getMaxSlots() + " ô)!");
            }
        }

        // Tru vat pham nguoi gui
        senderInv.setQuantity(senderRemaining);
        inventoryRepository.save(senderInv);

        // Cong vat pham nguoi nhan
        NxvInventory receiverInv = receiverInvOpt.orElseGet(() -> NxvInventory.builder()
                .user(receiver)
                .item(senderInv.getItem())
                .quantity(0)
                .build());
        receiverInv.setQuantity(receiverInv.getQuantity() + req.getQuantity());
        inventoryRepository.save(receiverInv);

        // Gui thong bao cho nguoi nhan
        String noteMsg = (req.getMessage() != null && !req.getMessage().isBlank())
                ? " Lời nhắn: \"" + req.getMessage().trim() + "\"" : "";
        notificationsRepository.save(NxvNotifications.builder()
                .user(receiver)
                .title("Quà tặng vật phẩm từ " + sender.getUsername())
                .content("Bạn vừa nhận được " + req.getQuantity() + "x [" + senderInv.getItem().getItemName() + "] từ " + sender.getUsername() + "." + noteMsg)
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("receiverUserId", receiver.getUserId());
        res.put("receiverUsername", receiver.getUsername());
        res.put("itemId", senderInv.getItem().getItemId());
        res.put("itemName", senderInv.getItem().getItemName());
        res.put("quantityGifted", req.getQuantity());
        res.put("senderRemainingQuantity", senderRemaining);
        return res;
    }

    // Helper: Them vat pham vao tui do (Dung chung cho Gacha, Marketplace, Admin)
    @Transactional
    public void addItemToUserInventory(Users user, NxvItems item, int quantity) {
        NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(user.getUserId(), item.getItemId())
                .orElseGet(() -> NxvInventory.builder()
                        .user(user)
                        .item(item)
                        .quantity(0)
                        .build());
        inv.setQuantity(inv.getQuantity() + quantity);
        inventoryRepository.save(inv);
    }

    private void assertUserAlive(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));
    }

    private NxvUserInventoryConfig getOrCreateInventoryConfig(Long userId) {
        return inventoryConfigRepository.findById(userId).orElseGet(() ->
                inventoryConfigRepository.save(NxvUserInventoryConfig.builder()
                        .userId(userId)
                        .maxSlots(50)
                        .build())
        );
    }

    public ItemDetailDto toItemDetailDto(NxvItems i) {
        String typeLabel = switch (i.getItemType() != null ? i.getItemType() : -1) {
            case NxvConstants.ITEM_EQUIPMENT -> "Trang bị (Equipment)";
            case NxvConstants.ITEM_CONSUMABLE -> "Vật phẩm tiêu hao (Consumable)";
            case NxvConstants.ITEM_AVATAR -> "Ảnh đại diện 2D tĩnh (Avatar)";
            case NxvConstants.ITEM_AVATAR_FRAME -> "Khung Avatar 2D tĩnh (Avatar Frame)";
            case NxvConstants.ITEM_PET_EGG -> "Trứng thú cưng (Pet Egg)";
            case NxvConstants.ITEM_TITLE -> "Danh hiệu (Title)";
            default -> "Vật phẩm khác";
        };

        return ItemDetailDto.builder()
                .itemId(i.getItemId())
                .itemName(i.getItemName())
                .itemType(i.getItemType())
                .itemTypeLabel(typeLabel)
                .imageUrl(i.getImageUrl())
                .equipSlotId(i.getEquipSlot() != null ? i.getEquipSlot().getSlotId() : null)
                .equipSlotName(i.getEquipSlot() != null ? i.getEquipSlot().getSlotName() : null)
                .equipSlotKey(i.getEquipSlot() != null ? i.getEquipSlot().getSlotKey() : null)
                .buffMaxHp(i.getBuffMaxHp())
                .buffMaxMp(i.getBuffMaxMp())
                .buffExpBonus(i.getBuffExpBonus())
                .buffGoldBonus(i.getBuffGoldBonus())
                .buffDeadlineDmgReduce(i.getBuffDeadlineDmgReduce())
                .healHp(i.getHealHp())
                .sellPrice(i.getSellPrice())
                .build();
    }
}
"@

# ==============================================================================
# 4. SINH REST CONTROLLER CHO MODULE 6A (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvInventoryController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvInventoryDtos.*;
import $basePkg.nxvservice.NxvInventoryService;
import $basePkg.nxvutils.NxvSecurityUtils;
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
"@

Write-Host "==> HOAN TAT SINH MODULE 6A (TASK 3: INVENTORY, EQUIP/UNEQUIP SP, CONSUMABLES, EXPAND, SELL, GIFT) THANH CONG!" -ForegroundColor Green