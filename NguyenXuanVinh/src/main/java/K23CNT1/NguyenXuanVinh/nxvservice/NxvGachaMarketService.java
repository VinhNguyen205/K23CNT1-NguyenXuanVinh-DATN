package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvGachaMarketDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
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