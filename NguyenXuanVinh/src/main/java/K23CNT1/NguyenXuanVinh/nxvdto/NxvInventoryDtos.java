package K23CNT1.NguyenXuanVinh.nxvdto;

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