package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvCart;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface nxvCartRepository extends JpaRepository<nxvCart, Integer> {
    // Tìm giỏ hàng của user
    Optional<nxvCart> findByUser(nxvUser user);
}