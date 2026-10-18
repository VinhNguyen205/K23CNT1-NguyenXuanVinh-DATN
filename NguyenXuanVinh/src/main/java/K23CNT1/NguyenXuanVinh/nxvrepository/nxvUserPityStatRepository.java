package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvBlindBox;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUserPityStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface nxvUserPityStatRepository extends JpaRepository<nxvUserPityStat, Integer> {
    // Tìm chỉ số bảo hiểm của user với 1 hộp
    Optional<nxvUserPityStat> findByUserAndBlindBox(nxvUser user, nxvBlindBox blindBox);
}