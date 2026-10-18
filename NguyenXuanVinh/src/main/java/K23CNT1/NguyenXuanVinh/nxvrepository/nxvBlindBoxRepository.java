package K23CNT1.NguyenXuanVinh.nxvrepository;
import K23CNT1.NguyenXuanVinh.nxventity.nxvBlindBox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface nxvBlindBoxRepository extends JpaRepository<nxvBlindBox, Integer> {
    List<nxvBlindBox> findByBoxNameContaining(String boxName);
}