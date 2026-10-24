package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvGachaDropRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvGachaDropRatesRepository extends JpaRepository<NxvGachaDropRates, Integer> {
    List<NxvGachaDropRates> findByBanner_BannerIdOrderByDropRateAsc(Integer bannerId);
}