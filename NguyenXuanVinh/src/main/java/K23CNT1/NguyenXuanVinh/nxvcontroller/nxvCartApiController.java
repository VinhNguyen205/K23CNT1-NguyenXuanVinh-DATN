package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvservice.nxvShoppingCartService; // Đã đổi tên
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class nxvCartApiController { // Đã đổi tên class

    @Autowired private nxvShoppingCartService nxvCartService; // Đã đổi tên type và variable

    // Thêm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Integer userId, @RequestParam Integer boxId) {
        try {
            // Đã gọi qua variable mới
            nxvCartService.addBoxToCart(userId, boxId, 1); // Mặc định thêm 1
            return ResponseEntity.ok("Đã thêm vào giỏ hàng!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa khỏi giỏ
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer itemId) {
        // Đã gọi qua variable mới
        nxvCartService.removeCartItem(itemId);
        return ResponseEntity.ok("Đã xóa!");
    }
}