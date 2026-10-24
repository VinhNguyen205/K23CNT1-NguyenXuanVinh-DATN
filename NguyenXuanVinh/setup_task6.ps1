# ==============================================================================
# TASKTALE / LIFERPG - TASK 6 AUTOMATED GENERATOR (MODULE 6B: GACHA & MARKETPLACE)
# Hoan thien Chuc nang #72 -> #77 & #85 -> #88: Gacha Banners, Pull x1 / x10,
# Weighted Drop Rate + Pet DropRate Buff, 50-Pull Pity Guarantee, Gacha History,
# Marketplace Listing, Buy Item with 5% Server Tax, Cancel Listing & Market History
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

Write-Host "==> Dang sinh Module 6B (Task 6: Gacha Box, Pity System & Marketplace 5% Tax) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 6B
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvGachaBannersRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvGachaBanners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvGachaBannersRepository extends JpaRepository<NxvGachaBanners, Integer> {
    List<NxvGachaBanners> findByIsActiveTrueOrderByBannerIdAsc();
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvGachaDropRatesRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvGachaDropRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvGachaDropRatesRepository extends JpaRepository<NxvGachaDropRates, Integer> {
    List<NxvGachaDropRates> findByBanner_BannerIdOrderByDropRateAsc(Integer bannerId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserPityCountersRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserPityCounters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserPityCountersRepository extends JpaRepository<NxvUserPityCounters, Long> {
    Optional<NxvUserPityCounters> findByUser_UserIdAndBanner_BannerId(Long userId, Integer bannerId);
    List<NxvUserPityCounters> findByUser_UserId(Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvGachaHistoryRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvGachaHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvGachaHistoryRepository extends JpaRepository<NxvGachaHistory, Long> {
    List<NxvGachaHistory> findTop100ByUser_UserIdOrderByPulledAtDesc(Long userId);
    List<NxvGachaHistory> findTop100ByOrderByPulledAtDesc();
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvMarketListingsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvMarketListings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvMarketListingsRepository extends JpaRepository<NxvMarketListings, Long> {
    List<NxvMarketListings> findByStatusOrderByCreatedAtDesc(Integer status);
    List<NxvMarketListings> findBySellerUser_UserIdOrderByCreatedAtDesc(Long sellerUserId);
    Optional<NxvMarketListings> findByListingIdAndSellerUser_UserId(Long listingId, Long sellerUserId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvMarketTransactionsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvMarketTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvMarketTransactionsRepository extends JpaRepository<NxvMarketTransactions, Long> {
    List<NxvMarketTransactions> findTop100ByBuyerUser_UserIdOrderByTxAtDesc(Long buyerUserId);
    List<NxvMarketTransactions> findTop100ByListing_SellerUser_UserIdOrderByTxAtDesc(Long sellerUserId);
    List<NxvMarketTransactions> findTop100ByOrderByTxAtDesc();
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserPetsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserPets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserPetsRepository extends JpaRepository<NxvUserPets, Long> {
    List<NxvUserPets> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    Optional<NxvUserPets> findFirstByUser_UserIdAndIsActiveTrueAndIsHibernatingFalseAndStageGreaterThanEqual(Long userId, Integer minStage);
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 6B (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvGachaMarketDtos.java" @"
package $basePkg.nxvdto;

import $basePkg.nxvdto.NxvInventoryDtos.ItemDetailDto;
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
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 6B (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvGachaMarketService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvGachaMarketDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvGachaMarketService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvItemsRepository itemsRepository;
    private final NxvInventoryRepository inventoryRepository;
    private final NxvEquippedItemsRepository equippedItemsRepository;
    private final NxvGachaBannersRepository gachaBannersRepository;
    private final NxvGachaDropRatesRepository gachaDropRatesRepository;
    private final NxvUserPityCountersRepository userPityCountersRepository;
    private final NxvGachaHistoryRepository gachaHistoryRepository;
    private final NxvMarketListingsRepository marketListingsRepository;
    private final NxvMarketTransactionsRepository marketTransactionsRepository;
    private final NxvUserPetsRepository userPetsRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;
    private final NxvInventoryService inventoryService;

    private static final SecureRandom RANDOM = new SecureRandom();
    public static final BigDecimal DEFAULT_MARKET_TAX_RATE = new BigDecimal("0.0500"); // 5% thue san

    // =========================================================================
    // PHAN 1: HE THONG HOP MU (GACHA BOX) & BAO HIEM PITY (#72 -> #77)
    // =========================================================================

    // Chuc nang #72: Hien thi danh sach Banner dang mo ban kem ty le Drop Rate va Pity Counter
    @Transactional
    public List<GachaBannerDto> getActiveBanners(Long userId) {
        ensureDefaultBannerSeeded();
        List<NxvGachaBanners> banners = gachaBannersRepository.findByIsActiveTrueOrderByBannerIdAsc();

        Map<Integer, NxvUserPityCounters> pityMap = new HashMap<>();
        if (userId != null) {
            for (NxvUserPityCounters p : userPityCountersRepository.findByUser_UserId(userId)) {
                pityMap.put(p.getBanner().getBannerId(), p);
            }
        }

        return banners.stream().map(b -> {
            NxvUserPityCounters pity = pityMap.get(b.getBannerId());
            int curPull = (pity != null && pity.getPullCount() != null) ? pity.getPullCount() : 0;
            int totPull = (pity != null && pity.getTotalPulls() != null) ? pity.getTotalPulls() : 0;
            int limit = (b.getPityLimit() != null && b.getPityLimit() > 0) ? b.getPityLimit() : 50;

            List<DropRateItemDto> dropTable = gachaDropRatesRepository
                    .findByBanner_BannerIdOrderByDropRateAsc(b.getBannerId())
                    .stream()
                    .map(dr -> DropRateItemDto.builder()
                            .dropRateId(dr.getDropRateId())
                            .dropRate(dr.getDropRate())
                            .dropPercent(dr.getDropRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).doubleValue())
                            .isFeatured(dr.getIsFeatured())
                            .item(inventoryService.toItemDetailDto(dr.getItem()))
                            .build())
                    .collect(Collectors.toList());

            return GachaBannerDto.builder()
                    .bannerId(b.getBannerId())
                    .bannerName(b.getBannerName())
                    .bannerImageUrl(b.getBannerImageUrl())
                    .costPerPull(b.getCostPerPull())
                    .costTenPulls(b.getCostPerPull() * 10)
                    .pityLimit(limit)
                    .isActive(b.getIsActive())
                    .userCurrentPullCount(curPull)
                    .userRemainingToPity(Math.max(1, limit - curPull))
                    .userTotalPulls(totPull)
                    .dropTable(dropTable)
                    .build();
        }).collect(Collectors.toList());
    }

    // Chuc nang #73, #74, #76, #77: Quay Hop mu x1 hoac x10 (Co Pity 50 lan & Buff DropRate tu Pet)
    @Transactional
    public GachaPullResponse pullGacha(Long userId, Integer bannerId, int pullTimes) {
        if (pullTimes != 1 && pullTimes != 10) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hệ thống chỉ hỗ trợ Mở 1 lần (x1) hoặc Mở 10 lần (x10)!");
        }

        Users user = getUserAlive(userId);
        NxvCharacters character = getCharacter(userId);
        ensureDefaultBannerSeeded();

        NxvGachaBanners banner = gachaBannersRepository.findById(bannerId)
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Banner Hộp mù đang mở bán với ID: " + bannerId));

        List<NxvGachaDropRates> dropRates = gachaDropRatesRepository.findByBanner_BannerIdOrderByDropRateAsc(bannerId);
        if (dropRates.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Banner này chưa được cấu hình danh sách vật phẩm rớt (Drop Rates)!");
        }

        long costPerPull = (banner.getCostPerPull() != null && banner.getCostPerPull() > 0) ? banner.getCostPerPull() : 100L;
        long totalCost = costPerPull * pullTimes;

        if (character.getGold() < totalCost) {
            throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                    "Không đủ Vàng để mở Hộp mù! Cần " + totalCost + " Gold (Hiện có: " + character.getGold() + " Gold).");
        }

        // Tru Vang & Ghi StatLog
        character.setGold(character.getGold() - totalCost);
        character.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(character);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(user)
                .logType("Gold")
                .delta(-totalCost)
                .sourceType("GachaPull_x" + pullTimes)
                .sourceId(bannerId.longValue())
                .createdAt(LocalDateTime.now())
                .build());

        // Lay bo dem Pity hien tai cua nguoi choi tren Banner nay
        NxvUserPityCounters pityCounter = userPityCountersRepository
                .findByUser_UserIdAndBanner_BannerId(userId, bannerId)
                .orElseGet(() -> NxvUserPityCounters.builder()
                        .user(user)
                        .banner(banner)
                        .pullCount(0)
                        .totalPulls(0)
                        .build());

        // Lay Buff tang ty le rot do hiem tu Pet dang dong hanh (#64, #76)
        BigDecimal petDropBonus = userPetsRepository
                .findFirstByUser_UserIdAndIsActiveTrueAndIsHibernatingFalseAndStageGreaterThanEqual(userId, 1)
                .map(up -> up.getSpecies().getPassiveDropRateBonus())
                .orElse(BigDecimal.ZERO);

        int pityLimit = (banner.getPityLimit() != null && banner.getPityLimit() > 0) ? banner.getPityLimit() : 50;
        List<NxvGachaDropRates> featuredPool = dropRates.stream()
                .filter(dr -> Boolean.TRUE.equals(dr.getIsFeatured()))
                .collect(Collectors.toList());
        if (featuredPool.isEmpty()) {
            featuredPool = List.of(dropRates.get(0));
        }

        List<PullResultItemDto> pulledResults = new ArrayList<>();

        for (int i = 1; i <= pullTimes; i++) {
            int nextPullCount = pityCounter.getPullCount() + 1;
            pityCounter.setTotalPulls(pityCounter.getTotalPulls() + 1);

            boolean isPityTriggered = (nextPullCount >= pityLimit);
            NxvGachaDropRates chosenDrop;

            if (isPityTriggered) {
                // Chuc nang #77: Cham moc Pity 50 -> Chac chan 100% ra vat pham Featured/Hiem
                chosenDrop = featuredPool.get(RANDOM.nextInt(featuredPool.size()));
                pityCounter.setPullCount(0);
            } else {
                // Chuc nang #76: Random co trong so + cong Buff tu Pet cho vat pham Featured
                chosenDrop = rollWeightedDropRate(dropRates, petDropBonus);
                if (Boolean.TRUE.equals(chosenDrop.getIsFeatured())) {
                    pityCounter.setPullCount(0); // No do hiem som cung reset Pity ve 0
                } else {
                    pityCounter.setPullCount(nextPullCount);
                }
            }

            NxvItems wonItem = chosenDrop.getItem();
            inventoryService.addItemToUserInventory(user, wonItem, 1);

            NxvGachaHistory history = gachaHistoryRepository.save(NxvGachaHistory.builder()
                    .user(user)
                    .banner(banner)
                    .item(wonItem)
                    .isPity(isPityTriggered)
                    .goldSpent((int) costPerPull)
                    .pulledAt(LocalDateTime.now())
                    .build());

            pulledResults.add(PullResultItemDto.builder()
                    .gachaHistoryId(history.getGachaHistoryId())
                    .pullIndex(i)
                    .isPityTriggered(isPityTriggered)
                    .isFeaturedItem(chosenDrop.getIsFeatured())
                    .goldSpent((int) costPerPull)
                    .pulledAt(history.getPulledAt())
                    .item(inventoryService.toItemDetailDto(wonItem))
                    .build());
        }

        userPityCountersRepository.save(pityCounter);

        return GachaPullResponse.builder()
                .bannerId(banner.getBannerId())
                .bannerName(banner.getBannerName())
                .pullCountRequested(pullTimes)
                .totalGoldSpent(totalCost)
                .remainingGold(character.getGold())
                .currentPityCounter(pityCounter.getPullCount())
                .pityLimit(pityLimit)
                .appliedPetDropRateBonus(petDropBonus)
                .pulledItems(pulledResults)
                .build();
    }

    // Chuc nang #75: Xem lich su Mo hop mu (Ca nhan hoac Toan Server)
    @Transactional(readOnly = true)
    public List<GachaHistoryDto> getGachaHistory(Long userId, boolean globalHistory) {
        List<NxvGachaHistory> logs = globalHistory
                ? gachaHistoryRepository.findTop100ByOrderByPulledAtDesc()
                : gachaHistoryRepository.findTop100ByUser_UserIdOrderByPulledAtDesc(userId);

        return logs.stream().map(h -> {
            Users u = h.getUser();
            NxvCharacters c = charactersRepository.findByUser_UserId(u.getUserId()).orElse(null);
            return GachaHistoryDto.builder()
                    .gachaHistoryId(h.getGachaHistoryId())
                    .userId(u.getUserId())
                    .username(u.getUsername())
                    .characterName(c != null ? c.getCharacterName() : u.getUsername())
                    .bannerId(h.getBanner().getBannerId())
                    .bannerName(h.getBanner().getBannerName())
                    .itemId(h.getItem().getItemId())
                    .itemName(h.getItem().getItemName())
                    .itemType(h.getItem().getItemType())
                    .imageUrl(h.getItem().getImageUrl())
                    .isPity(h.getIsPity())
                    .goldSpent(h.getGoldSpent())
                    .pulledAt(h.getPulledAt())
                    .build();
        }).collect(Collectors.toList());
    }

    // =========================================================================
    // PHAN 2: THI TRUONG GIAO DICH (MARKETPLACE) & THUE 5% (#85 -> #88)
    // =========================================================================

    // Chuc nang #85: Dang ban vat pham ao tren Cho giao dich (Marketplace)
    @Transactional
    public MarketListingDto createMarketListing(Long sellerUserId, CreateMarketListingRequest req) {
        Users seller = getUserAlive(sellerUserId);
        NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(sellerUserId, req.getItemId())
                .filter(i -> i.getQuantity() >= req.getQuantity())
                .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY,
                        "Bạn không có đủ số lượng vật phẩm này trong túi đồ để đăng bán!"));

        boolean isEquipped = equippedItemsRepository.existsByUser_UserIdAndItem_ItemId(sellerUserId, req.getItemId());
        int remainingQty = inv.getQuantity() - req.getQuantity();
        if (isEquipped && remainingQty < 1) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Vật phẩm này đang được trang bị trên người! Vui lòng tháo trang bị trước khi đăng bán chiếc cuối cùng.");
        }

        // Tam giu vat pham (tru khoi tui do khi dang rao ban tren cho)
        inv.setQuantity(remainingQty);
        inventoryRepository.save(inv);

        NxvMarketListings listing = marketListingsRepository.save(NxvMarketListings.builder()
                .sellerUser(seller)
                .item(inv.getItem())
                .quantity(req.getQuantity())
                .pricePerUnit(req.getPricePerUnit())
                .status(0) // 0 = Active
                .taxRate(DEFAULT_MARKET_TAX_RATE)
                .createdAt(LocalDateTime.now())
                .build());

        return toMarketListingDto(listing);
    }

    // Xem danh sach vat pham dang rao ban tren Marketplace
    @Transactional(readOnly = true)
    public List<MarketListingDto> getActiveMarketListings(Integer itemType, String keyword) {
        return marketListingsRepository.findByStatusOrderByCreatedAtDesc(0).stream()
                .filter(l -> itemType == null || Objects.equals(l.getItem().getItemType(), itemType))
                .filter(l -> keyword == null || keyword.isBlank()
                        || l.getItem().getItemName().toLowerCase().contains(keyword.trim().toLowerCase()))
                .map(this::toMarketListingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MarketListingDto> getMyMarketListings(Long userId) {
        return marketListingsRepository.findBySellerUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toMarketListingDto)
                .collect(Collectors.toList());
    }

    // Go / Huy bai dang ban tren Marketplace -> Hoan tra vat pham ve tui do
    @Transactional
    public MarketListingDto cancelMarketListing(Long sellerUserId, Long listingId) {
        Users seller = getUserAlive(sellerUserId);
        NxvMarketListings listing = marketListingsRepository.findByListingIdAndSellerUser_UserId(listingId, sellerUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy bài đăng bán của bạn với ID: " + listingId));

        if (listing.getStatus() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bài đăng bán này đã được mua hoặc đã hủy trước đó!");
        }

        listing.setStatus(2); // 2 = Cancelled
        listing = marketListingsRepository.save(listing);

        inventoryService.addItemToUserInventory(seller, listing.getItem(), listing.getQuantity());
        return toMarketListingDto(listing);
    }

    // Chuc nang #86 & #88: Mua vat pham tu nguoi choi khac bang Gold & Thu thue giao dich 5% ve server
    @Transactional
    public MarketTransactionDto buyMarketListing(Long buyerUserId, Long listingId) {
        Users buyer = getUserAlive(buyerUserId);
        NxvMarketListings listing = marketListingsRepository.findById(listingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy bài đăng bán ID: " + listingId));

        if (listing.getStatus() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Vật phẩm này đã được người khác mua hoặc đã bị gỡ khỏi chợ!");
        }

        Users seller = listing.getSellerUser();
        if (Objects.equals(seller.getUserId(), buyerUserId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể tự mua vật phẩm do chính mình đăng bán! Hãy dùng nút Hủy bán để lấy lại đồ.");
        }

        long totalPrice = (long) listing.getPricePerUnit() * listing.getQuantity();
        NxvCharacters buyerChar = getCharacter(buyerUserId);
        if (buyerChar.getGold() < totalPrice) {
            throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                    "Không đủ Vàng để mua! Cần " + totalPrice + " Gold (Hiện có: " + buyerChar.getGold() + " Gold).");
        }

        // Chuc nang #88: Tinh thue giao dich (TaxRate = 5%) thu hoi ve server
        BigDecimal taxRate = (listing.getTaxRate() != null) ? listing.getTaxRate() : DEFAULT_MARKET_TAX_RATE;
        long taxGold = BigDecimal.valueOf(totalPrice)
                .multiply(taxRate)
                .setScale(0, RoundingMode.FLOOR)
                .longValue();
        long sellerNetGold = Math.max(0L, totalPrice - taxGold);

        // 1. Tru Vang nguoi mua
        buyerChar.setGold(buyerChar.getGold() - totalPrice);
        buyerChar.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(buyerChar);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(buyer)
                .logType("Gold")
                .delta(-totalPrice)
                .sourceType("MarketBuy")
                .sourceId(listingId)
                .createdAt(LocalDateTime.now())
                .build());

        // 2. Cong Vang thuc nhan (sau thue 5%) cho nguoi ban
        NxvCharacters sellerChar = getCharacter(seller.getUserId());
        sellerChar.setGold(sellerChar.getGold() + sellerNetGold);
        sellerChar.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(sellerChar);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(seller)
                .logType("Gold")
                .delta(sellerNetGold)
                .sourceType("MarketSellNet")
                .sourceId(listingId)
                .createdAt(LocalDateTime.now())
                .build());

        // 3. Chuyen vat pham vao Tui do nguoi mua
        inventoryService.addItemToUserInventory(buyer, listing.getItem(), listing.getQuantity());

        // 4. Cap nhat trang thai Listing & Luu Transaction (#87)
        LocalDateTime now = LocalDateTime.now();
        listing.setStatus(1); // 1 = Sold
        listing.setSoldAt(now);
        marketListingsRepository.save(listing);

        NxvMarketTransactions tx = marketTransactionsRepository.save(NxvMarketTransactions.builder()
                .listing(listing)
                .buyerUser(buyer)
                .totalPrice(totalPrice)
                .txAt(now)
                .build());

        // 5. Gui thong bao cho nguoi ban
        notificationsRepository.save(NxvNotifications.builder()
                .user(seller)
                .title("Đã bán vật phẩm trên Marketplace!")
                .content(buyer.getUsername() + " vừa mua " + listing.getQuantity() + "x [" + listing.getItem().getItemName()
                        + "] với tổng giá " + totalPrice + " Gold (Thuế sàn 5%: -" + taxGold + " Gold, Thực nhận: +" + sellerNetGold + " Gold).")
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(now)
                .build());

        return toMarketTransactionDto(tx);
    }

    // Chuc nang #87: Xem lich su giao dich tren Marketplace
    @Transactional(readOnly = true)
    public List<MarketTransactionDto> getMarketTransactions(Long userId, String mode) {
        List<NxvMarketTransactions> list;
        if ("BOUGHT".equalsIgnoreCase(mode)) {
            list = marketTransactionsRepository.findTop100ByBuyerUser_UserIdOrderByTxAtDesc(userId);
        } else if ("SOLD".equalsIgnoreCase(mode)) {
            list = marketTransactionsRepository.findTop100ByListing_SellerUser_UserIdOrderByTxAtDesc(userId);
        } else {
            list = marketTransactionsRepository.findTop100ByOrderByTxAtDesc();
        }
        return list.stream().map(this::toMarketTransactionDto).collect(Collectors.toList());
    }

    // --- Helpers ---
    private NxvGachaDropRates rollWeightedDropRate(List<NxvGachaDropRates> rates, BigDecimal petDropBonus) {
        double bonus = (petDropBonus != null && petDropBonus.doubleValue() > 0) ? petDropBonus.doubleValue() : 0.0;
        double totalWeight = 0.0;
        double[] weights = new double[rates.size()];

        for (int i = 0; i < rates.size(); i++) {
            NxvGachaDropRates dr = rates.get(i);
            double w = dr.getDropRate() != null ? dr.getDropRate().doubleValue() : 0.01;
            if (Boolean.TRUE.equals(dr.getIsFeatured()) && bonus > 0) {
                w = w * (1.0 + bonus);
            }
            weights[i] = Math.max(0.0001, w);
            totalWeight += weights[i];
        }

        double roll = RANDOM.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (int i = 0; i < rates.size(); i++) {
            cumulative += weights[i];
            if (roll <= cumulative) {
                return rates.get(i);
            }
        }
        return rates.get(rates.size() - 1);
    }

    @Transactional
    public void ensureDefaultBannerSeeded() {
        if (gachaBannersRepository.count() > 0) return;

        NxvGachaBanners defaultBanner = gachaBannersRepository.save(NxvGachaBanners.builder()
                .bannerName("Hộp Mù Khởi Nguyên Pixel")
                .bannerImageUrl("/images/banners/starter_blindbox.png")
                .costPerPull(100)
                .pityLimit(50)
                .isActive(true)
                .build());

        List<NxvItems> allItems = itemsRepository.findAll();
        if (!allItems.isEmpty()) {
            for (int i = 0; i < allItems.size(); i++) {
                NxvItems item = allItems.get(i);
                boolean featured = (i == 0 || item.getBuffMaxHp() >= 50 || item.getBuffGoldBonus().doubleValue() >= 0.10);
                BigDecimal rate = featured ? new BigDecimal("0.050000") : new BigDecimal("0.300000");

                gachaDropRatesRepository.save(NxvGachaDropRates.builder()
                        .banner(defaultBanner)
                        .item(item)
                        .dropRate(rate)
                        .isFeatured(featured)
                        .build());
            }
        }
    }

    private MarketListingDto toMarketListingDto(NxvMarketListings l) {
        long total = (long) l.getPricePerUnit() * l.getQuantity();
        BigDecimal taxRate = l.getTaxRate() != null ? l.getTaxRate() : DEFAULT_MARKET_TAX_RATE;
        long taxGold = BigDecimal.valueOf(total).multiply(taxRate).setScale(0, RoundingMode.FLOOR).longValue();
        long netGold = Math.max(0L, total - taxGold);

        Users seller = l.getSellerUser();
        NxvCharacters c = charactersRepository.findByUser_UserId(seller.getUserId()).orElse(null);

        String stLabel = switch (l.getStatus() != null ? l.getStatus() : 0) {
            case 1 -> "SOLD (Đã bán)";
            case 2 -> "CANCELLED (Đã hủy)";
            default -> "ACTIVE (Đang rao bán)";
        };

        return MarketListingDto.builder()
                .listingId(l.getListingId())
                .sellerUserId(seller.getUserId())
                .sellerUsername(seller.getUsername())
                .sellerCharacterName(c != null ? c.getCharacterName() : seller.getUsername())
                .quantity(l.getQuantity())
                .pricePerUnit(l.getPricePerUnit())
                .totalPrice(total)
                .taxRate(taxRate)
                .estimatedTaxGold(taxGold)
                .estimatedSellerNetGold(netGold)
                .status(l.getStatus())
                .statusLabel(stLabel)
                .createdAt(l.getCreatedAt())
                .soldAt(l.getSoldAt())
                .item(inventoryService.toItemDetailDto(l.getItem()))
                .build();
    }

    private MarketTransactionDto toMarketTransactionDto(NxvMarketTransactions tx) {
        NxvMarketListings l = tx.getListing();
        long total = tx.getTotalPrice();
        BigDecimal taxRate = l.getTaxRate() != null ? l.getTaxRate() : DEFAULT_MARKET_TAX_RATE;
        long taxGold = BigDecimal.valueOf(total).multiply(taxRate).setScale(0, RoundingMode.FLOOR).longValue();
        long netGold = Math.max(0L, total - taxGold);

        return MarketTransactionDto.builder()
                .marketTxId(tx.getMarketTxId())
                .listingId(l.getListingId())
                .sellerUserId(l.getSellerUser().getUserId())
                .sellerUsername(l.getSellerUser().getUsername())
                .buyerUserId(tx.getBuyerUser().getUserId())
                .buyerUsername(tx.getBuyerUser().getUsername())
                .itemId(l.getItem().getItemId())
                .itemName(l.getItem().getItemName())
                .quantity(l.getQuantity())
                .pricePerUnit(l.getPricePerUnit())
                .totalPrice(total)
                .taxRate(taxRate)
                .serverTaxGold(taxGold)
                .sellerNetGold(netGold)
                .txAt(tx.getTxAt())
                .build();
    }

    private Users getUserAlive(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
        return u;
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));
    }
}
"@

# ==============================================================================
# 4. SINH REST CONTROLLER CHO MODULE 6B (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvGachaMarketController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvGachaMarketDtos.*;
import $basePkg.nxvservice.NxvGachaMarketService;
import $basePkg.nxvutils.NxvSecurityUtils;
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
"@

Write-Host "==> HOAN TAT SINH MODULE 6B (TASK 6: GACHA BOX, PITY 50, MARKETPLACE 5% TAX) THANH CONG!" -ForegroundColor Green