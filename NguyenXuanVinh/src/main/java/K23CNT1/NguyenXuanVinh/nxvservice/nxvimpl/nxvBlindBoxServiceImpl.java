package K23CNT1.NguyenXuanVinh.nxvservice.nxvimpl;

import K23CNT1.NguyenXuanVinh.nxvdto.nxvOpenBoxResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.nxvSellItemResponse;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import K23CNT1.NguyenXuanVinh.nxventity.nxvBlindBox;
import K23CNT1.NguyenXuanVinh.nxventity.nxvBoxItem;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUserInventory;
import K23CNT1.NguyenXuanVinh.nxventity.nxvTransaction;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUserPityStat;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserRepository;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvBlindBoxRepository;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvBoxItemRepository;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserInventoryRepository;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvTransactionRepository;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserPityStatRepository;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvBlindBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class nxvBlindBoxServiceImpl implements nxvBlindBoxService {

    @Autowired private nxvUserRepository userRepository;
    @Autowired private nxvBlindBoxRepository blindBoxRepository;
    @Autowired private nxvBoxItemRepository boxItemRepository;
    @Autowired private nxvUserInventoryRepository inventoryRepository;
    @Autowired private nxvTransactionRepository transactionRepository;
    @Autowired private nxvUserPityStatRepository pityStatsRepository;

    @Override
    @Transactional
    public nxvOpenBoxResponse buyAndOpenBox(Integer userId, Integer boxId) {
        nxvUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        nxvBlindBox box = blindBoxRepository.findById(boxId)
                .orElseThrow(() -> new RuntimeException("Hộp không tồn tại!"));

        if (user.getWalletBalance().compareTo(box.getPrice()) < 0) {
            throw new RuntimeException("Số dư không đủ! Cần thêm " +
                    (box.getPrice().subtract(user.getWalletBalance())) + "đ");
        }

        // 1. Trừ tiền
        user.setWalletBalance(user.getWalletBalance().subtract(box.getPrice()));
        userRepository.save(user);

        // 2. Lưu lịch sử giao dịch
        nxvTransaction trans = new nxvTransaction();
        trans.setUser(user);
        trans.setAmount(box.getPrice().negate());
        trans.setTransactionType("BUY_BOX");
        trans.setDescription("Mua hộp: " + box.getBoxName());
        transactionRepository.save(trans);

        // 3. Quay thưởng (Logic mới chuẩn xác hơn)
        nxvBoxItem wonItem = determineItem(user, box);

        // 4. Lưu kho
        nxvUserInventory inventory = new nxvUserInventory();
        inventory.setUser(user);
        inventory.setBoxItem(wonItem);
        inventory.setStatus("IN_STORAGE");
        inventory.setObtainedDate(LocalDateTime.now());
        inventoryRepository.save(inventory);

        // 5. Tính Pity
        nxvUserPityStat stats = getOrCreatePityStats(user, box);
        int spinsRemaining = 100 - stats.getSpinsWithoutS();
        if (spinsRemaining < 0) spinsRemaining = 0;

        // 6. Trả về kết quả (Đã thêm currentBalance)
        return nxvOpenBoxResponse.builder()
                .itemName(wonItem.getItemName())
                .itemImage(wonItem.getImageUrl())
                .rarity(wonItem.getRarityLevel())
                .spinsUntilPity(spinsRemaining)
                .currentBalance(user.getWalletBalance()) // <--- FIX LỖI 0đ TẠI ĐÂY
                .build();
    }

    @Override
    @Transactional
    public nxvSellItemResponse sellItem(Integer userId, Integer inventoryId) {
        nxvUserInventory item = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại!"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Vật phẩm này không phải của bạn!");
        }

        BigDecimal sellPrice = item.getBoxItem().getMarketValue().multiply(BigDecimal.valueOf(0.9));
        nxvUser user = item.getUser();
        user.setWalletBalance(user.getWalletBalance().add(sellPrice));
        userRepository.save(user);

        nxvTransaction trans = new nxvTransaction();
        trans.setUser(user);
        trans.setAmount(sellPrice);
        trans.setTransactionType("SELL_ITEM");
        trans.setDescription("Bán vật phẩm: " + item.getBoxItem().getItemName());
        transactionRepository.save(trans);

        inventoryRepository.delete(item);

        return nxvSellItemResponse.builder()
                .message("Đã bán " + item.getBoxItem().getItemName() + " thành công!")
                .soldPrice(sellPrice)
                .newBalance(user.getWalletBalance())
                .build();
    }

    // --- LOGIC RANDOM CHUẨN (Fix lỗi auto S) ---
    private nxvBoxItem determineItem(nxvUser user, nxvBlindBox box) {
        nxvUserPityStat stats = getOrCreatePityStats(user, box);
        List<nxvBoxItem> allItems = boxItemRepository.findByBlindBox(box);

        // 1. Check Bảo Hiểm (Pity)
        if (stats.getSpinsWithoutS() >= 99) {
            List<nxvBoxItem> sItems = allItems.stream()
                    .filter(i -> "S".equals(i.getRarityLevel()))
                    .collect(Collectors.toList());
            if (!sItems.isEmpty()) {
                Collections.shuffle(sItems);
                resetPity(stats);
                return sItems.get(0);
            }
        }

        // 2. Logic Random theo trọng số (Weighted Random)
        // Tính tổng xác suất thực tế trong DB (ví dụ tổng là 100, hoặc 1, hoặc 50...)
        double totalProbability = allItems.stream()
                .mapToDouble(nxvBoxItem::getProbability)
                .sum();

        double randomValue = Math.random() * totalProbability;
        double cumulativeProb = 0.0;
        nxvBoxItem selectedItem = null;

        for (nxvBoxItem item : allItems) {
            cumulativeProb += item.getProbability();
            if (randomValue <= cumulativeProb) {
                selectedItem = item;
                break;
            }
        }

        // Fallback an toàn (chỉ khi lỗi data nghiêm trọng)
        if (selectedItem == null && !allItems.isEmpty()) {
            selectedItem = allItems.get(0);
        }

        // 3. Cập nhật Pity
        if ("S".equals(selectedItem.getRarityLevel())) {
            resetPity(stats);
        } else {
            stats.setSpinsWithoutS(stats.getSpinsWithoutS() + 1);
            pityStatsRepository.save(stats);
        }

        return selectedItem;
    }

    private void resetPity(nxvUserPityStat stats) {
        stats.setSpinsWithoutS(0);
        stats.setLastSRewardDate(LocalDateTime.now());
        pityStatsRepository.save(stats);
    }

    private nxvUserPityStat getOrCreatePityStats(nxvUser user, nxvBlindBox box) {
        nxvUserPityStat stats = pityStatsRepository.findByUserAndBlindBox(user, box).orElse(null);
        if (stats == null) {
            stats = new nxvUserPityStat();
            stats.setUser(user);
            stats.setBlindBox(box);
            stats.setSpinsWithoutS(0);
            pityStatsRepository.save(stats);
        }
        return stats;
    }
}