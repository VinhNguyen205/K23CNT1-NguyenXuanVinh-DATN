package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.nxvUser; // Đã đổi tên Entity
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserRepository; // Đã đổi tên Repository
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class nxvUserApiController { // Đã đổi tên Class

    @Autowired private nxvUserRepository nxvUserRepository; // Đã đổi tên Type và variable

    // API Cập nhật thông tin cá nhân
    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestParam Integer userId,
                                           @RequestParam String fullName,
                                           @RequestParam String email,
                                           @RequestParam String address,
                                           @RequestParam String phone,
                                           HttpSession session) {
        try {
            // Tìm và ép kiểu về nxvUser
            nxvUser user = nxvUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            // Cập nhật thông tin
            user.setFullName(fullName);
            user.setEmail(email);
            // Cập nhật thêm SĐT và Địa chỉ
            user.setAddress(address);
            user.setPhoneNumber(phone);

            nxvUserRepository.save(user);

            // Cập nhật lại session
            session.setAttribute("currentUser", user);

            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}