package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class nxvShipmentService {

    private final nxvShipmentRequestRepository shipmentRequestRepository;
    private final nxvShipmentDetailRepository shipmentDetailRepository; // Bạn cần tạo repo này (trống cũng được)
    private final nxvUserInventoryRepository inventoryRepository;
    private final nxvUserRepository userRepository;
    private final nxvTransactionRepository transactionRepository;

    private final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(20000); // Phí 20k

    @Transactional
    public void createShipmentRequest(Integer userId, String name, String phone, String address, String note, List<Integer> inventoryIds) throws Exception {
        nxvUser user = userRepository.findById(userId).orElseThrow();

        // 1. Kiểm tra số dư (Phải đủ 20k)
        if (user.getWalletBalance().compareTo(SHIPPING_FEE) < 0) {
            throw new Exception("Số dư không đủ thanh toán phí ship (20.000đ)");
        }

        // 2. Trừ tiền & Lưu lịch sử giao dịch
        user.setWalletBalance(user.getWalletBalance().subtract(SHIPPING_FEE));
        userRepository.save(user);

        nxvTransaction trans = new nxvTransaction();
        trans.setUser(user);
        trans.setAmount(SHIPPING_FEE.negate()); // Số âm
        trans.setTransactionType("SHIPPING_FEE");
        trans.setDescription("Phí vận chuyển đơn hàng");
        transactionRepository.save(trans);

        // 3. Tạo yêu cầu Ship
        nxvShipmentRequest req = new nxvShipmentRequest();
        req.setUser(user);
        req.setReceiverName(name);
        req.setPhoneNumber(phone);
        req.setAddress(address);
        req.setNote(note);
        req.setShipmentStatus("PENDING"); // Chờ xử lý
        req.setRequestDate(LocalDateTime.now());
        nxvShipmentRequest savedReq = shipmentRequestRepository.save(req);

        // 4. Update Inventory & Tạo Detail
        for (Integer invId : inventoryIds) {
            nxvUserInventory item = inventoryRepository.findById(invId).orElse(null);
            if (item != null && "IN_STORAGE".equals(item.getStatus())) {
                // Đổi trạng thái item để không bán/ship lại được nữa
                item.setStatus("REQUESTED_SHIP");
                inventoryRepository.save(item);

                // Lưu chi tiết đơn ship
                nxvShipmentDetail detail = new nxvShipmentDetail();
                detail.setShipmentRequest(savedReq);
                detail.setUserInventory(item);
                // Cần thêm repo cho Detail: nxvShipmentDetailRepository
                shipmentDetailRepository.save(detail);
            }
        }
    }

    public List<nxvShipmentRequest> getUserHistory(nxvUser user) {
        return shipmentRequestRepository.findByUserOrderByRequestDateDesc(user);
    }
}