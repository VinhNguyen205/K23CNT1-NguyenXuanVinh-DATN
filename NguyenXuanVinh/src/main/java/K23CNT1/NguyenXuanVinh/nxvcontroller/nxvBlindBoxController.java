package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.nxvOpenBoxResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.nxvSellItemResponse;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserRepository;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvBlindBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/blindbox")
public class nxvBlindBoxController {

    @Autowired
    private nxvBlindBoxService nxvBlindBoxService;

    @Autowired
    private nxvUserRepository nxvUserRepository; // Thêm cái này để lấy số dư mới nhất

    // API 1: Mua và Mở hộp
    // URL: POST http://localhost:8080/api/blindbox/open?userId=1&boxId=1
    @PostMapping("/open")
    public ResponseEntity<?> openBox(@RequestParam Integer userId, @RequestParam Integer boxId) {
        try {
            nxvOpenBoxResponse result = nxvBlindBoxService.buyAndOpenBox(userId, boxId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 2: Bán lại vật phẩm (Buyback) - ĐÃ NÂNG CẤP ĐỂ FIX DELAY TIỀN
    // URL: POST http://localhost:8080/api/blindbox/sell?userId=1&inventoryId=1
    @PostMapping("/sell")
    public ResponseEntity<?> sellItem(@RequestParam Integer userId, @RequestParam Integer inventoryId) {
        try {
            // 1. Gọi Service thực hiện bán (Xóa đồ, cộng tiền trong DB)
            nxvSellItemResponse resultDTO = nxvBlindBoxService.sellItem(userId, inventoryId);

            // 2. Lấy lại User từ DB để đảm bảo số dư là mới nhất (Real-time)
            nxvUser updatedUser = nxvUserRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

            // 3. Đóng gói dữ liệu trả về cho Frontend (JS cần 'newBalance' để update giao diện)
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", resultDTO.getMessage()); // "Bán thành công..."

            // QUAN TRỌNG: Trả về số dư mới nhất để JS cập nhật ngay lập tức
            response.put("newBalance", updatedUser.getWalletBalance());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Trả về lỗi dạng JSON để JS hiển thị SweetAlert đẹp
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}