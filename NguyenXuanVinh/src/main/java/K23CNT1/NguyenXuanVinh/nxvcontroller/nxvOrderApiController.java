package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvservice.nxvOrderService; // Đã đổi tên
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class nxvOrderApiController { // Đã đổi tên Class

    @Autowired private nxvOrderService nxvOrderService; // Đã đổi tên Type và variable

    // API Thanh toán
    // POST /api/order/checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam Integer userId,
                                      @RequestParam String name,
                                      @RequestParam String phone,
                                      @RequestParam String address) {
        try {
            // Gọi qua Service mới
            nxvOrderService.placeOrder(userId, name, phone, address);
            return ResponseEntity.ok("Đặt hàng thành công! Mã đơn hàng đã được tạo.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}