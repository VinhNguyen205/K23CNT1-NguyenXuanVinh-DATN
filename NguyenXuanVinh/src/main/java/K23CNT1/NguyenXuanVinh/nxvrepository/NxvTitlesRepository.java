package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvTitles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NxvTitlesRepository extends JpaRepository<NxvTitles, Integer> {
    java.util.Optional<NxvTitles> findFirstByTitleName(String titleName);
}