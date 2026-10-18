package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvCategory;
import K23CNT1.NguyenXuanVinh.nxventity.nxvProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvProductRepository extends JpaRepository<nxvProduct, Integer> {
    List<nxvProduct> findByCategory(nxvCategory category);
    List<nxvProduct> findByProductNameContaining(String keyword);
    List<nxvProduct> findAllByOrderByCreatedAtDesc();
}