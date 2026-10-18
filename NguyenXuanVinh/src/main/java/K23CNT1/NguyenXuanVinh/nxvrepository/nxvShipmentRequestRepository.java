package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvShipmentRequest;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser; // Nhớ import User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvShipmentRequestRepository extends JpaRepository<nxvShipmentRequest, Integer> {

    List<nxvShipmentRequest> findAllByOrderByRequestDateDesc();

    List<nxvShipmentRequest> findByUserOrderByRequestDateDesc(nxvUser user);
}