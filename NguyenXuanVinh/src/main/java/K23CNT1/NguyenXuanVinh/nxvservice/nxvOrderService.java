package K23CNT1.NguyenXuanVinh.nxvservice;
import K23CNT1.NguyenXuanVinh.nxventity.nxvOrder;

public interface nxvOrderService {
    nxvOrder placeOrder(Integer userId, String receiverName, String phoneNumber, String address);
}