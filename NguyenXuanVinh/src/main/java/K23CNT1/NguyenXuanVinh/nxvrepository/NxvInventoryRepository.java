package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvInventoryRepository extends JpaRepository<NxvInventory, Long> {
    List<NxvInventory> findByUser_UserId(Long userId);
    List<NxvInventory> findByUser_UserIdAndQuantityGreaterThanOrderByInventoryIdDesc(Long userId, Integer minQuantity);
    Optional<NxvInventory> findByUser_UserIdAndItem_ItemId(Long userId, Integer itemId);
    long countByUser_UserIdAndQuantityGreaterThan(Long userId, Integer minQuantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_EquipItem @UserID = :userId, @ItemID = :itemId", nativeQuery = true)
    void executeEquipItemSp(@Param("userId") Long userId, @Param("itemId") Integer itemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_UnequipItem @UserID = :userId, @SlotID = :slotId", nativeQuery = true)
    void executeUnequipItemSp(@Param("userId") Long userId, @Param("slotId") Integer slotId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "EXEC NxvSp_UseConsumable @UserID = :userId, @ItemID = :itemId", nativeQuery = true)
    void executeUseConsumableSp(@Param("userId") Long userId, @Param("itemId") Integer itemId);
}