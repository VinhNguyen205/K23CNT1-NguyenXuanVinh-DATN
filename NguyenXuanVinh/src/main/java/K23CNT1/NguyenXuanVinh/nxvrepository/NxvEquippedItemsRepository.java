package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvEquippedItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvEquippedItemsRepository extends JpaRepository<NxvEquippedItems, Long> {
    List<NxvEquippedItems> findByUser_UserId(Long userId);
    Optional<NxvEquippedItems> findByUser_UserIdAndSlot_SlotId(Long userId, Integer slotId);
    boolean existsByUser_UserIdAndItem_ItemId(Long userId, Integer itemId);
}