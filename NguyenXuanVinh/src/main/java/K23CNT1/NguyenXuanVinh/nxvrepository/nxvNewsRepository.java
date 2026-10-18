package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvNewsRepository extends JpaRepository<nxvNews, Integer> {
    List<nxvNews> findAllByOrderByPublishedAtDesc();
    List<nxvNews> findByTitleContaining(String keyword);
}