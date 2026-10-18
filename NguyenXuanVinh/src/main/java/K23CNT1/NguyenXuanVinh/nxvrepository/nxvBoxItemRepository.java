package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvBlindBox;
import K23CNT1.NguyenXuanVinh.nxventity.nxvBoxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvBoxItemRepository extends JpaRepository<nxvBoxItem, Integer> {
    List<nxvBoxItem> findByBlindBox(nxvBlindBox blindBox);

    List<nxvBoxItem> findByStockQuantityLessThanOrderByStockQuantityAsc(Integer stockLimit);
}