package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserRepository;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvAdminService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class nxvAdminController {

    private final nxvAdminService adminService;
    private final nxvUserRepository userRepository;

    // --- HELPER: CHECK ADMIN PERMISSIONS ---
    // Hàm kiểm tra quyền Admin từ session
    private nxvUser getAdminUser(HttpSession session) {
        Object sessionUser = session.getAttribute("currentUser");
        if (sessionUser instanceof nxvUser) {
            nxvUser user = (nxvUser) sessionUser;
            // Chỉ cho phép nếu IsAdmin = true
            if (Boolean.TRUE.equals(user.getIsAdmin())) return user;
        }
        return null;
    }

    // ==========================================
    // 1. DASHBOARD (TRANG CHỦ ADMIN)
    // ==========================================
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model, HttpSession session) {
        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        // Truyền dữ liệu thống kê ra View
        model.addAttribute("nxvUser", admin);
        model.addAttribute("stats", adminService.getDashboardStats());
        model.addAttribute("topUsers", adminService.getTopDepositors());

        // Dữ liệu cho các Modal (Thêm box, Nạp tiền...)
        model.addAttribute("boxes", adminService.getAllBoxes());
        model.addAttribute("listCategories", adminService.getAllCategories());
        model.addAttribute("users", userRepository.findAll());

        return "admin/dashboard";
    }

    // ==========================================
    // 2. SHIPMENT MANAGEMENT (QUẢN LÝ VẬN ĐƠN)
    // Thay thế cho Order Management cũ
    // ==========================================
    @GetMapping("/orders")
    public String viewShipments(Model model, HttpSession session,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status) {

        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        // Lấy danh sách YÊU CẦU GIAO HÀNG (ShipmentRequest)
        // Gọi hàm searchShipments từ Service (đã viết ở bước trước)
        List<nxvShipmentRequest> shipments = adminService.searchShipments(keyword, status);

        // Truyền dữ liệu ra View
        // Lưu ý: Vẫn dùng tên biến "listOrders" để file HTML không bị lỗi (do lười sửa HTML)
        // Nhưng bản chất dữ liệu bên trong là ShipmentRequest
        model.addAttribute("listOrders", shipments);
        model.addAttribute("nxvUser", admin);

        // Giữ lại giá trị bộ lọc để hiển thị trên giao diện
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentStatus", status);

        return "admin/orders";
    }

    // API Cập Nhật Trạng Thái Vận Đơn (Duyệt/Hủy/Giao)
    @PostMapping("/orders/update-status")
    @ResponseBody
    public ResponseEntity<?> updateShipmentStatus(@RequestParam("id") Integer id,
                                                  @RequestParam("status") String status,
                                                  HttpSession session) {

        if (getAdminUser(session) == null) return ResponseEntity.status(401).body("Unauthorized");

        // Gọi Service để update trạng thái trong DB
        boolean updated = adminService.updateShipmentStatus(id, status);

        if (updated) {
            return ResponseEntity.ok("Cập nhật thành công!");
        } else {
            return ResponseEntity.badRequest().body("Không tìm thấy vận đơn hoặc lỗi hệ thống.");
        }
    }

    // ==========================================
    // 3. INVENTORY & ALERTS (KHO HÀNG & CẢNH BÁO)
    // ==========================================
    @GetMapping("/inventory")
    public String viewInventory(Model model, HttpSession session) {
        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("lowStockItems", adminService.getLowStockItems());
        model.addAttribute("nxvUser", admin);
        return "admin/inventory";
    }

    // API Cập nhật số lượng tồn kho nhanh
    @PostMapping("/inventory/update")
    @ResponseBody
    public ResponseEntity<?> updateStock(@RequestParam Integer itemId, @RequestParam Integer quantity, HttpSession session) {
        if (getAdminUser(session) == null) return ResponseEntity.status(401).body("Unauthorized");

        adminService.updateStock(itemId, quantity);
        return ResponseEntity.ok("Updated stock successfully");
    }

    // ==========================================
    // 4. USER MANAGEMENT (NẠP TIỀN CHO USER)
    // ==========================================
    @PostMapping("/add-money")
    public String addMoney(@RequestParam Integer userId, @RequestParam BigDecimal amount, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.addMoneyToUser(userId, amount);
        return "redirect:/admin"; // Quay lại Dashboard sau khi nạp xong
    }

    // ==========================================
    // 5. BLIND BOX MANAGEMENT (QUẢN LÝ SẢN PHẨM)
    // ==========================================
    @PostMapping("/save-box")
    public String saveBox(@ModelAttribute nxvBlindBox box, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.saveBox(box);
        return "redirect:/admin";
    }

    @GetMapping("/delete-box/{id}")
    public String deleteBox(@PathVariable Integer id, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        try {
            adminService.deleteBox(id);
        } catch (Exception e) {
            // Có thể thêm thông báo lỗi nếu cần (vd: Box đang có người mua không xóa được)
        }
        return "redirect:/admin";
    }

    // ==========================================
    // 6. CATEGORY MANAGEMENT (QUẢN LÝ DANH MỤC)
    // ==========================================
    @GetMapping("/categories")
    public String viewCategories(Model model, HttpSession session) {
        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("listCategories", adminService.getAllCategories());
        model.addAttribute("newCategory", new nxvCategory());
        model.addAttribute("nxvUser", admin);
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute nxvCategory category, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    // ==========================================
    // 7. NEWS MANAGEMENT (QUẢN LÝ TIN TỨC)
    // ==========================================
    @GetMapping("/news")
    public String viewNews(Model model, HttpSession session) {
        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("listNews", adminService.getAllNews());
        model.addAttribute("newNews", new nxvNews());
        model.addAttribute("nxvUser", admin);
        return "admin/news";
    }

    @PostMapping("/news/save")
    public String saveNews(@ModelAttribute nxvNews news, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.saveNews(news);
        return "redirect:/admin/news";
    }

    @GetMapping("/news/delete/{id}")
    public String deleteNews(@PathVariable Integer id, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.deleteNews(id);
        return "redirect:/admin/news";
    }

    // ==========================================
    // 8. BANNER MANAGEMENT (QUẢN LÝ BANNER QUẢNG CÁO)
    // ==========================================
    @GetMapping("/banners")
    public String viewBanners(Model model, HttpSession session) {
        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("listBanners", adminService.getAllBanners());
        model.addAttribute("newBanner", new nxvBanner());
        model.addAttribute("nxvUser", admin);
        return "admin/banners";
    }

    @PostMapping("/banners/save")
    public String saveBanner(@ModelAttribute nxvBanner banner, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.saveBanner(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/banners/delete/{id}")
    public String deleteBanner(@PathVariable Integer id, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.deleteBanner(id);
        return "redirect:/admin/banners";
    }

    // ==========================================
    // 9. FEEDBACK MANAGEMENT (QUẢN LÝ PHẢN HỒI)
    // ==========================================
    @GetMapping("/feedbacks")
    public String viewFeedbacks(Model model, HttpSession session) {
        nxvUser admin = getAdminUser(session);
        if (admin == null) return "redirect:/login";

        model.addAttribute("listFeedbacks", adminService.getAllFeedbacks());
        model.addAttribute("nxvUser", admin);
        return "admin/feedbacks";
    }

    @PostMapping("/feedbacks/reply")
    public String replyFeedback(@RequestParam Integer id, @RequestParam String replyContent, HttpSession session) {
        if (getAdminUser(session) == null) return "redirect:/login";

        adminService.replyFeedback(id, replyContent);
        return "redirect:/admin/feedbacks";
    }
}