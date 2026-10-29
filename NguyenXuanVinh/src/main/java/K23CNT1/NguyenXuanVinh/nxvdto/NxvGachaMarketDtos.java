package K23CNT1.NguyenXuanVinh.nxvdto;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvInventoryDtos.ItemDetailDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NxvGachaMarketDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DropRateItemDto {
        private Integer dropRateId;
        private BigDecimal dropRate;
        private Double dropPercent;
        private Boolean isFeatured;
        private ItemDetailDto item;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GachaBannerDto {
        private Integer bannerId;
        private String bannerName;
        private String bannerImageUrl;
        private Integer costPerPull;
        private Integer costTenPulls;
        private Integer pityLimit;
        private Boolean isActive;
        private Integer userCurrentPullCount;
        private Integer userRemainingToPity;
        private Integer userTotalPulls;
        private List<DropRateItemDto> dropTable;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PullResultItemDto {
        private Long gachaHistoryId;
        private Integer pullIndex;
        private Boolean isPityTriggered;
        private Boolean isFeaturedItem;
        private Integer goldSpent;
        private LocalDateTime pulledAt;
        private ItemDetailDto item;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GachaPullResponse {
        private Integer bannerId;
        private String bannerName;
        private Integer pullCountRequested;
        private Long totalGoldSpent;
        private Long remainingGold;
        private Integer currentPityCounter;
        private Integer pityLimit;
        private BigDecimal appliedPetDropRateBonus;
        private List<PullResultItemDto> pulledItems;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GachaHistoryDto {
        private Long gachaHistoryId;
        private Long userId;
        private String username;
        private String characterName;
        private Integer bannerId;
        private String bannerName;
        private Integer itemId;
        private String itemName;
        private Integer itemType;
        private String imageUrl;
        private Boolean isPity;
        private Integer goldSpent;
        private LocalDateTime pulledAt;
    }

    // --- MARKETPLACE DTOS ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateMarketListingRequest {
        @NotNull(message = "itemId không được để trống")
        private Integer itemId;

        @NotNull(message = "Số lượng đăng bán không được để trống")
        @Min(value = 1, message = "Số lượng đăng bán tối thiểu là 1")
        private Integer quantity;

        @NotNull(message = "Đơn giá (pricePerUnit) không được để trống")
        @Min(value = 1, message = "Đơn giá tối thiểu là 1 Gold")
        private Integer pricePerUnit;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MarketListingDto {
        private Long listingId;
        private Long sellerUserId;
        private String sellerUsername;
        private String sellerCharacterName;
        private Integer quantity;
        private Integer pricePerUnit;
        private Long totalPrice;
        private BigDecimal taxRate;
        private Long estimatedTaxGold;
        private Long estimatedSellerNetGold;
        private Integer status; // 0=Active, 1=Sold, 2=Cancelled
        private String statusLabel;
        private LocalDateTime createdAt;
        private LocalDateTime soldAt;
        private ItemDetailDto item;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MarketTransactionDto {
        private Long marketTxId;
        private Long listingId;
        private Long sellerUserId;
        private String sellerUsername;
        private Long buyerUserId;
        private String buyerUsername;
        private Integer itemId;
        private String itemName;
        private Integer quantity;
        private Integer pricePerUnit;
        private Long totalPrice;
        private BigDecimal taxRate;
        private Long serverTaxGold;
        private Long sellerNetGold;
        private LocalDateTime txAt;
    }
}