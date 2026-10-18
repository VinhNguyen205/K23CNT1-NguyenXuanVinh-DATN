package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvCart;
import K23CNT1.NguyenXuanVinh.nxventity.nxvCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvCartItemRepository extends JpaRepository<nxvCartItem, Integer> {
    // Lấy tất cả item trong giỏ
    List<nxvCartItem> findByCart(nxvCart cart);

    // Xóa sạch giỏ
    void deleteByCart(nxvCart cart);
}