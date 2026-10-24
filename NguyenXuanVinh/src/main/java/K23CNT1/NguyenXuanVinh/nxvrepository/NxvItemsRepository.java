package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvItemsRepository extends JpaRepository<NxvItems, Integer> {
    List<NxvItems> findByItemTypeOrderByItemIdAsc(Integer itemType);
}