package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface nxvCategoryRepository extends JpaRepository<nxvCategory, Integer> {
    nxvCategory findByCategoryName(String categoryName);
}