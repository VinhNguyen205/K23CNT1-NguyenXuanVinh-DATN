package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class nxvWebController {

    // --- REPOSITORIES (Khai báo final để Inject tự động) ---
    private final nxvBlindBoxRepository nxvBlindBoxRepository;
    private final nxvUserRepository nxvUserRepository;
    private final nxvUserInventoryRepository nxvUserInventoryRepository;
    private final nxvCartRepository nxvCartRepository;
    private final nxvCartItemRepository nxvCartItemRepository;
    private final nxvTransactionRepository nxvTransactionRepository;
    private final nxvBoxItemRepository nxvBoxItemRepository;
    private final nxvOrderRepository nxvOrderRepository;

    // Repos bổ sung cho giao diện
    private final nxvBannerRepository nxvBannerRepository;
    private final nxvNewsRepository nxvNewsRepository;
    private final nxvCategoryRepository nxvCategoryRepository;
    private final nxvUserPityStatRepository nxvUserPityStatRepository; // [MỚI] Cho bảo hiểm S

    // Helper: Lấy user hiện tại và refresh từ DB
    private nxvUser getCurrentUser(HttpSession session) {
        Object sessionUser = session.getAttribute("currentUser");
        if (sessionUser instanceof nxvUser) {
            nxvUser user = (nxvUser) sessionUser;
            user = nxvUserRepository.findById(user.getUserId()).orElse(null);
            if (user != null) {
                session.setAttribute("currentUser", user);
                return user;
            }
        }
        return null;
    }

    // --- 1. TRANG CHỦ (HOME) ---
    @GetMapping("/")
    public String home(Model model, HttpSession session, @RequestParam(required = false) String keyword) {
        model.addAttribute("nxvUser", getCurrentUser(session));

        // Tìm kiếm
        List<nxvBlindBox> boxes;
        if (keyword != null && !keyword.isEmpty()) {
            boxes = nxvBlindBoxRepository.findByBoxNameContaining(keyword);
        } else {
            boxes = nxvBlindBoxRepository.findAll();
        }
        model.addAttribute("boxes", boxes);
        model.addAttribute("keyword", keyword);

        // Load Banner, News, Category
        model.addAttribute("banners", nxvBannerRepository.findAllByOrderByDisplayOrderAsc());
        model.addAttribute("newsList", nxvNewsRepository.findAllByOrderByPublishedAtDesc());
        model.addAttribute("categories", nxvCategoryRepository.findAll());

        // [MỚI] Lấy Top 5 Đại Gia (Để hiển thị BXH ở Index)
        // Cần import org.springframework.data.domain.PageRequest;
        model.addAttribute("topUsers", nxvTransactionRepository.findTopDepositors(PageRequest.of(0, 5)));

        return "index";
    }

    // --- 2. CHI TIẾT SẢN PHẨM (BOX DETAIL) ---
    @GetMapping("/box/{id}")
    public String boxDetail(@PathVariable Integer id, Model model, HttpSession session) {
        nxvUser user = getCurrentUser(session);
        model.addAttribute("nxvUser", user);

        nxvBlindBox box = nxvBlindBoxRepository.findById(id).orElse(null);
        if (box == null) return "redirect:/";

        model.addAttribute("box", box);

        // Lấy list item và sắp xếp Rank S lên đầu
        List<nxvBoxItem> items = nxvBoxItemRepository.findByBlindBox(box);
        if (items != null) {
            items.sort((a, b) -> {
                String order = "SABCD";
                String rankStrA = a.getRarityLevel() != null ? a.getRarityLevel() : "Z";
                String rankStrB = b.getRarityLevel() != null ? b.getRarityLevel() : "Z";
                int rankA = order.indexOf(rankStrA);
                int rankB = order.indexOf(rankStrB);
                if (rankA == -1) rankA = 99;
                if (rankB == -1) rankB = 99;
                return Integer.compare(rankA, rankB);
            });
        }
        model.addAttribute("items", items);

        // [MỚI] Tính toán Pity (Bảo hiểm S-Tier)
        int remainingToS = 50; // Mặc định 50
        if (user != null) {
            nxvUserPityStat stat = nxvUserPityStatRepository.findByUserAndBlindBox(user, box).orElse(null);
            if (stat != null) {
                remainingToS = 50 - stat.getSpinsWithoutS();
                if (remainingToS < 0) remainingToS = 0;
            }
        }
        model.addAttribute("remainingToS", remainingToS);

        return "box-detail";
    }

    // --- 3. KHO ĐỒ (INVENTORY) ---
    @GetMapping("/inventory")
    public String inventory(Model model, HttpSession session) {
        nxvUser user = getCurrentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("nxvUser", user);
        model.addAttribute("inventoryList", nxvUserInventoryRepository.findByUserAndStatus(user, "IN_STORAGE"));
        return "inventory";
    }

    // --- 4. GIỎ HÀNG (CART) ---
    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        nxvUser user = getCurrentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("nxvUser", user);

        nxvCart cart = nxvCartRepository.findByUser(user).orElse(null);
        List<nxvCartItem> cartItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (cart != null) {
            cartItems = nxvCartItemRepository.findByCart(cart);
            for (nxvCartItem item : cartItems) {
                if (item.getBlindBox() != null) {
                    BigDecimal price = item.getBlindBox().getPrice();
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                    totalAmount = totalAmount.add(price.multiply(quantity));
                }
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        return "cart";
    }

    // --- 5. HỒ SƠ & LỊCH SỬ (PROFILE) ---
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        nxvUser user = getCurrentUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("nxvUser", user);
        model.addAttribute("orders", nxvOrderRepository.findByUserOrderByOrderDateDesc(user));
        return "profile";
    }
}