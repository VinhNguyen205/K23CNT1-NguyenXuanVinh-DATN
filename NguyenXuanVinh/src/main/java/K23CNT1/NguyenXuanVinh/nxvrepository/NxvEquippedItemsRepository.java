package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvEquippedItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvEquippedItemsRepository extends JpaRepository<NxvEquippedItems, Long> {
    List<NxvEquippedItems> findByUser_UserId(Long userId);
}