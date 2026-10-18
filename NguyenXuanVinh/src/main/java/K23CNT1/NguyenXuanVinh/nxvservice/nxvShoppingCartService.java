package K23CNT1.NguyenXuanVinh.nxvservice;
import K23CNT1.NguyenXuanVinh.nxventity.nxvCart;

public interface nxvShoppingCartService {
    void addBoxToCart(Integer userId, Integer boxId, int quantity);
    nxvCart getCart(Integer userId);
    void removeCartItem(Integer cartItemId);
    void clearCart(Integer userId);
}