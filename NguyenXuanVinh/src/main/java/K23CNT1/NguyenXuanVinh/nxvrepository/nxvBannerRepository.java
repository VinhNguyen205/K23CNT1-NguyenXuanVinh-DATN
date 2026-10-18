package K23CNT1.NguyenXuanVinh.nxvrepository;
import K23CNT1.NguyenXuanVinh.nxventity.nxvBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface nxvBannerRepository extends JpaRepository<nxvBanner, Integer> {
    List<nxvBanner> findAllByOrderByDisplayOrderAsc(); // Lấy banner theo thứ tự cài đặt
}