package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface nxvUserInventoryRepository extends JpaRepository<nxvUserInventory, Integer> {

    List<nxvUserInventory> findByUserAndStatus(nxvUser user, String status);

    Optional<nxvUserInventory> findByInventoryIdAndUser(Integer inventoryId, nxvUser user);

    List<nxvUserInventory> findByUserOrderByCreatedAtDesc(nxvUser user);

    List<nxvUserInventory> findTop5ByBoxItem_RarityLevelOrderByCreatedAtDesc(String rarityLevel);
}