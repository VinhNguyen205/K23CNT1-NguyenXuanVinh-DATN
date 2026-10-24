package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvInventoryDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y váº­t pháº©m ID: " + itemId));
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
                    "KhÃ´ng Ä‘á»§ VÃ ng! Cáº§n " + totalCost + " Gold Ä‘á»ƒ má»Ÿ rá»™ng thÃªm " + addedSlots + " Ã´ tÃºi Ä‘á»“.");
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
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY, "Sá»‘ lÆ°á»£ng váº­t pháº©m trong tÃºi Ä‘á»“ khÃ´ng Ä‘á»§ Ä‘á»ƒ bÃ¡n!"));

        boolean isEquipped = equippedItemsRepository.existsByUser_UserIdAndItem_ItemId(userId, req.getItemId());
        int remaining = inv.getQuantity() - req.getQuantity();
        if (isEquipped && remaining < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Váº­t pháº©m nÃ y Ä‘ang Ä‘Æ°á»£c máº·c trÃªn ngÆ°á»i! Vui lÃ²ng thÃ¡o trang bá»‹ trÆ°á»›c khi bÃ¡n chiáº¿c cuá»‘i cÃ¹ng.");
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
            throw new AppException(ErrorCode.BAD_REQUEST, "KhÃ´ng thá»ƒ tá»± gá»­i táº·ng váº­t pháº©m cho chÃ­nh mÃ¬nh!");
        }

        Users sender = usersRepository.findById(senderUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i gá»­i!"));
        Users receiver = usersRepository.findById(req.getReceiverUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i chÆ¡i nháº­n quÃ !"));

        NxvInventory senderInv = inventoryRepository.findByUser_UserIdAndItem_ItemId(senderUserId, req.getItemId())
                .filter(i -> i.getQuantity() >= req.getQuantity())
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY, "Báº¡n khÃ´ng cÃ³ Ä‘á»§ sá»‘ lÆ°á»£ng váº­t pháº©m nÃ y Ä‘á»ƒ táº·ng!"));

        boolean isEquipped = equippedItemsRepository.existsByUser_UserIdAndItem_ItemId(senderUserId, req.getItemId());
        int senderRemaining = senderInv.getQuantity() - req.getQuantity();
        if (isEquipped && senderRemaining < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Váº­t pháº©m nÃ y Ä‘ang Ä‘Æ°á»£c trang bá»‹! Vui lÃ²ng thÃ¡o ra trÆ°á»›c khi táº·ng chiáº¿c cuá»‘i cÃ¹ng.");
        }

        // Kiem tra tui do cua nguoi nhan
        Optional<NxvInventory> receiverInvOpt = inventoryRepository.findByUser_UserIdAndItem_ItemId(receiver.getUserId(), req.getItemId());
        if (receiverInvOpt.isEmpty() || receiverInvOpt.get().getQuantity() == 0) {
            NxvUserInventoryConfig receiverCfg = getOrCreateInventoryConfig(receiver.getUserId());
            long receiverUsedSlots = inventoryRepository.countByUser_UserIdAndQuantityGreaterThan(receiver.getUserId(), 0);
            if (receiverUsedSlots >= receiverCfg.getMaxSlots()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "TÃºi Ä‘á»“ cá»§a ngÆ°á»i nháº­n Ä‘Ã£ Ä‘áº§y (" + receiverCfg.getMaxSlots() + " Ã´)!");
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
                ? " Lá»i nháº¯n: \"" + req.getMessage().trim() + "\"" : "";
        notificationsRepository.save(NxvNotifications.builder()
                .user(receiver)
                .title("QuÃ  táº·ng váº­t pháº©m tá»« " + sender.getUsername())
                .content("Báº¡n vá»«a nháº­n Ä‘Æ°á»£c " + req.getQuantity() + "x [" + senderInv.getItem().getItemName() + "] tá»« " + sender.getUsername() + "." + noteMsg)
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n váº­t!"));
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
            case NxvConstants.ITEM_EQUIPMENT -> "Trang bá»‹ (Equipment)";
            case NxvConstants.ITEM_CONSUMABLE -> "Váº­t pháº©m tiÃªu hao (Consumable)";
            case NxvConstants.ITEM_AVATAR -> "áº¢nh Ä‘áº¡i diá»‡n 2D tÄ©nh (Avatar)";
            case NxvConstants.ITEM_AVATAR_FRAME -> "Khung Avatar 2D tÄ©nh (Avatar Frame)";
            case NxvConstants.ITEM_PET_EGG -> "Trá»©ng thÃº cÆ°ng (Pet Egg)";
            case NxvConstants.ITEM_TITLE -> "Danh hiá»‡u (Title)";
            default -> "Váº­t pháº©m khÃ¡c";
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