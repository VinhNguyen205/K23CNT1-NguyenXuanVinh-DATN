package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserInventoryRepository;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvShipmentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/shipment")
@RequiredArgsConstructor
public class nxvShipmentController {

    private final nxvShipmentService shipmentService;
    private final nxvUserInventoryRepository inventoryRepository;

    // --- HELPER: LẤY USER TỪ SESSION ---
    private nxvUser getCurrentUser(HttpSession session) {
        return (nxvUser) session.getAttribute("currentUser");
    }

    // ==========================================
    // 1. TRANG TẠO YÊU CẦU SHIP (CHỌN VẬT PHẨM)
    // ==========================================
    @GetMapping("/create")
    public String createShipmentPage(Model model, HttpSession session) {
        // 1. Kiểm tra đăng nhập
        nxvUser user = getCurrentUser(session);
        if (user == null) return "redirect:/login";

        // 2. Lấy danh sách vật phẩm đang có trong kho (IN_STORAGE)
        // Lưu ý: Đảm bảo Repository có hàm findByUserAndStatus
        List<nxvUserInventory> items = inventoryRepository.findByUserAndStatus(user, "IN_STORAGE");

        // 3. Truyền dữ liệu sang View
        model.addAttribute("items", items);
        model.addAttribute("nxvUser", user);

        return "shipment/create"; // Trả về file shipment/create.html
    }

    // ==========================================
    // 2. XỬ LÝ FORM GỬI YÊU CẦU SHIP
    // ==========================================
    @PostMapping("/submit")
    public String submitShipment(@RequestParam("receiverName") String receiverName,
                                 @RequestParam("phoneNumber") String phoneNumber,
                                 @RequestParam("address") String address,
                                 @RequestParam(value = "note", required = false) String note,
                                 @RequestParam(value = "selectedItems", required = false) List<Integer> selectedItems,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra đăng nhập
        nxvUser user = getCurrentUser(session);
        if (user == null) return "redirect:/login";

        // 2. Validate: Phải chọn ít nhất 1 món đồ
        if (selectedItems == null || selectedItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất 1 vật phẩm để giao!");
            return "redirect:/shipment/create";
        }

        // 3. Gọi Service để xử lý logic nghiệp vụ (Trừ tiền, Tạo đơn, Update kho)
        try {
            shipmentService.createShipmentRequest(user.getUserId(), receiverName, phoneNumber, address, note, selectedItems);

            // 4. Thành công -> Thông báo và chuyển sang trang lịch sử
            redirectAttributes.addFlashAttribute("success", "Tạo yêu cầu thành công! Phí vận chuyển 20.000đ đã được trừ vào ví.");
            return "redirect:/shipment/history";

        } catch (Exception e) {
            // 5. Thất bại (Ví dụ: Không đủ tiền, Lỗi DB...) -> Quay lại trang tạo
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/shipment/create";
        }
    }

    // ==========================================
    // 3. TRANG LỊCH SỬ SHIP HÀNG
    // ==========================================
    @GetMapping("/history")
    public String shipmentHistory(Model model, HttpSession session) {
        // 1. Kiểm tra đăng nhập
        nxvUser user = getCurrentUser(session);
        if (user == null) return "redirect:/login";

        // 2. Lấy danh sách lịch sử yêu cầu của User
        List<nxvShipmentRequest> history = shipmentService.getUserHistory(user);

        // 3. Truyền dữ liệu sang View
        model.addAttribute("requests", history); // Biến 'requests' dùng trong vòng lặp th:each
        model.addAttribute("nxvUser", user);

        return "shipment/history"; // Trả về file shipment/history.html
    }
}