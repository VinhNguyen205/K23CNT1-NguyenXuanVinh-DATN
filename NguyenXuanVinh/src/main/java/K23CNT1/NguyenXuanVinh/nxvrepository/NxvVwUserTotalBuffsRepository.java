package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvVwUserTotalBuffs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NxvVwUserTotalBuffsRepository extends JpaRepository<NxvVwUserTotalBuffs, Long> {
}