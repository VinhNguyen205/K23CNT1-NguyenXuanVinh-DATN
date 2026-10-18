package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.nxvDashboardStats;
import K23CNT1.NguyenXuanVinh.nxvdto.nxvTopUserDTO;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class nxvAdminService {

    // --- KHAI BÁO CÁC REPOSITORY ---
    private final nxvUserRepository userRepository;
    private final nxvTransactionRepository transactionRepository;
    private final nxvBlindBoxRepository boxRepository;
    private final nxvShipmentRepository shipmentRepository; // Sử dụng ShipmentRepository thay vì Order
    private final nxvCategoryRepository categoryRepository;
    private final nxvNewsRepository newsRepository;
    private final nxvBannerRepository bannerRepository;
    private final nxvFeedbackRepository feedbackRepository;
    private final nxvBoxItemRepository boxItemRepository;

    // ==========================================
    // 1. DASHBOARD & THỐNG KÊ
    // ==========================================
    public nxvDashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();

        // Tính tổng tiền nạp (Tránh lỗi null)
        BigDecimal deposit = transactionRepository.sumAmountByType("DEPOSIT");
        BigDecimal adminDeposit = transactionRepository.sumAmountByType("ADMIN_DEPOSIT");
        BigDecimal totalDeposit = (deposit != null ? deposit : BigDecimal.ZERO)
                .add(adminDeposit != null ? adminDeposit : BigDecimal.ZERO);

        // Tính doanh thu thực (Tổng tiền mua box)
        BigDecimal spent = transactionRepository.sumAmountByType("BUY_BOX");
        BigDecimal totalSpent = (spent != null ? spent : BigDecimal.ZERO).abs();

        return nxvDashboardStats.builder()
                .totalUsers(totalUsers)
                .totalDeposit(totalDeposit)
                .totalSpent(totalSpent)
                .build();
    }

    public List<nxvTopUserDTO> getTopDepositors() {
        return transactionRepository.findTopDepositors(PageRequest.of(0, 5));
    }

    // ==========================================
    // 2. QUẢN LÝ VẬN ĐƠN (SHIPMENT REQUESTS) - QUAN TRỌNG
    // ==========================================

    // Tìm kiếm và lọc danh sách yêu cầu giao hàng
    public List<nxvShipmentRequest> searchShipments(String keyword, String status) {
        String searchKey = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String searchStatus = (status == null || status.trim().isEmpty()) ? null : status;

        // Gọi Repository để tìm kiếm theo tiêu chí
        return shipmentRepository.searchShipments(searchStatus, searchKey);
    }

    // Cập nhật trạng thái vận đơn (Duyệt, Giao, Hoàn thành, Hủy)
    @Transactional
    public boolean updateShipmentStatus(Integer shipmentId, String status) {
        nxvShipmentRequest shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment != null) {
            shipment.setShipmentStatus(status);
            shipmentRepository.save(shipment);
            return true;
        }
        return false;
    }

    // ==========================================
    // 3. QUẢN LÝ USER (NẠP TIỀN)
    // ==========================================
    @Transactional
    public void addMoneyToUser(Integer userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;

        nxvUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);

        // Ghi lại lịch sử giao dịch
        nxvTransaction t = new nxvTransaction();
        t.setUser(user);
        t.setAmount(amount);
        t.setTransactionType("ADMIN_DEPOSIT");
        t.setDescription("Admin nạp tiền: " + String.format("%,.0f", amount) + "đ");
        t.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(t);
    }

    // ==========================================
    // 4. QUẢN LÝ SẢN PHẨM (BLIND BOX)
    // ==========================================
    public List<nxvBlindBox> getAllBoxes() {
        return boxRepository.findAll();
    }

    @Transactional
    public void saveBox(nxvBlindBox box) {
        if (box.getCreatedAt() == null) {
            box.setCreatedAt(LocalDateTime.now());
        }
        if (box.getImageUrl() == null || box.getImageUrl().trim().isEmpty()) {
            box.setImageUrl("/images/box-default.jpg");
        }
        if (box.getIsActive() == null) {
            box.setIsActive(true);
        }
        boxRepository.save(box);
    }

    @Transactional
    public void deleteBox(Integer id) {
        boxRepository.deleteById(id);
    }

    // ==========================================
    // 5. QUẢN LÝ DANH MỤC (CATEGORY)
    // ==========================================
    public List<nxvCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public void saveCategory(nxvCategory cat) {
        if (cat.getIsActive() == null) {
            cat.setIsActive(true);
        }
        categoryRepository.save(cat);
    }

    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }

    // ==========================================
    // 6. QUẢN LÝ TIN TỨC (NEWS)
    // ==========================================
    public List<nxvNews> getAllNews() {
        return newsRepository.findAllByOrderByPublishedAtDesc();
    }

    public void saveNews(nxvNews news) {
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }
        newsRepository.save(news);
    }

    public void deleteNews(Integer id) {
        newsRepository.deleteById(id);
    }

    // ==========================================
    // 7. QUẢN LÝ BANNER
    // ==========================================
    public List<nxvBanner> getAllBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    public void saveBanner(nxvBanner banner) {
        if (banner.getCreatedAt() == null) {
            banner.setCreatedAt(LocalDateTime.now());
        }
        bannerRepository.save(banner);
    }

    public void deleteBanner(Integer id) {
        bannerRepository.deleteById(id);
    }

    // ==========================================
    // 8. QUẢN LÝ PHẢN HỒI (FEEDBACK)
    // ==========================================
    public List<nxvFeedback> getAllFeedbacks() {
        return feedbackRepository.findAllByOrderBySentAtDesc();
    }

    @Transactional
    public void replyFeedback(Integer id, String replyContent) {
        nxvFeedback fb = feedbackRepository.findById(id).orElse(null);
        if (fb != null) {
            fb.setReplyContent(replyContent);
            fb.setStatus("RESOLVED");
            fb.setRepliedAt(LocalDateTime.now());
            feedbackRepository.save(fb);
        }
    }

    // ==========================================
    // 9. QUẢN LÝ KHO HÀNG (INVENTORY ALERTS)
    // ==========================================
    public List<nxvBoxItem> getLowStockItems() {
        // Lấy các item có số lượng tồn kho thấp (< 10)
        return boxItemRepository.findByStockQuantityLessThanOrderByStockQuantityAsc(10);
    }

    @Transactional
    public void updateStock(Integer itemId, Integer quantity) {
        nxvBoxItem item = boxItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            // Đảm bảo số lượng không âm
            item.setStockQuantity(Math.max(quantity, 0));
            boxItemRepository.save(item);
        }
    }
}