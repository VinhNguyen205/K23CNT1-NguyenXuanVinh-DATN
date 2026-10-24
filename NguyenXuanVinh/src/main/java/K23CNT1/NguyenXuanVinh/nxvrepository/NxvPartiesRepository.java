package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvParties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvPartiesRepository extends JpaRepository<NxvParties, Long> {
    List<NxvParties> findByIsActiveTrueOrderByCreatedAtDesc();
}