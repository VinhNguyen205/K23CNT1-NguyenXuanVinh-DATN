package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.nxvBlindBox; // Đã đổi tên Entity
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;      // Đã đổi tên Entity
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvBlindBoxRepository; // Đã đổi tên Repository
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserRepository;      // Đã đổi tên Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // Báo hiệu đây là API trả về dữ liệu (JSON)
@RequestMapping("/api/test") // Đường dẫn gốc
public class nxvTestConnectionController { // Đã đổi tên Class

    @Autowired
    private nxvUserRepository nxvUserRepository; // Đã đổi tên Type và variable

    @Autowired
    private nxvBlindBoxRepository nxvBlindBoxRepository; // Đã đổi tên Type và variable

    // Test 1: Lấy danh sách User
    // Truy cập: http://localhost:8080/api/test/users
    @GetMapping("/users")
    public List<nxvUser> getAllUsers() { // Type trả về đã đổi tên
        return nxvUserRepository.findAll();
    }

    // Test 2: Lấy danh sách Hộp Blind Box
    // Truy cập: http://localhost:8080/api/test/boxes
    @GetMapping("/boxes")
    public List<nxvBlindBox> getAllBoxes() { // Type trả về đã đổi tên
        return nxvBlindBoxRepository.findAll();
    }
}