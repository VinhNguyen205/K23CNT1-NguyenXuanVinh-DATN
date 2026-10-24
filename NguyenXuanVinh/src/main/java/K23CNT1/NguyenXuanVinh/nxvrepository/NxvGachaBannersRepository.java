package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvGachaBanners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvGachaBannersRepository extends JpaRepository<NxvGachaBanners, Integer> {
    List<NxvGachaBanners> findByIsActiveTrueOrderByBannerIdAsc();
}