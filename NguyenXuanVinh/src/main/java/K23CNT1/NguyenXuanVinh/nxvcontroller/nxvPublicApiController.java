package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.nxvUserInventory;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class nxvPublicApiController {

    private final nxvUserInventoryRepository inventoryRepository;

    @GetMapping("/winners")
    public List<Map<String, String>> getLatestWinners() {
        // --- SỬA ĐỔI QUAN TRỌNG ---
        // Gọi hàm chuẩn Spring Data JPA đã khai báo trong Repository
        // Tham số "S" nghĩa là chỉ lấy các món có độ hiếm là S
        List<nxvUserInventory> list = inventoryRepository.findTop5ByBoxItem_RarityLevelOrderByCreatedAtDesc("S");

        List<Map<String, String>> result = new ArrayList<>();

        for (nxvUserInventory item : list) {
            // Kiểm tra null để tránh lỗi server nếu dữ liệu bị thiếu
            if (item.getUser() == null || item.getBoxItem() == null) continue;

            Map<String, String> map = new HashMap<>();

            // Xử lý che tên bảo mật (VD: Vinh -> Vi***)
            String username = item.getUser().getUsername();
            String hiddenName = (username != null && username.length() > 2)
                    ? username.substring(0, 2) + "***"
                    : username + "***";

            map.put("user", hiddenName);
            map.put("item", item.getBoxItem().getItemName());

            // Lấy tên hộp (Check null phòng hờ)
            String boxName = (item.getBoxItem().getBlindBox() != null)
                    ? item.getBoxItem().getBlindBox().getBoxName()
                    : "Hộp Bí Ẩn";
            map.put("box", boxName);

            result.add(map);
        }
        return result;
    }
}