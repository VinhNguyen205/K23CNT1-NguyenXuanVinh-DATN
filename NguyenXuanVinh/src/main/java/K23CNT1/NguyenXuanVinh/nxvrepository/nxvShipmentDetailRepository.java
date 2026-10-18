package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.nxvShipmentDetail;
import K23CNT1.NguyenXuanVinh.nxventity.nxvShipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface nxvShipmentDetailRepository extends JpaRepository<nxvShipmentDetail, Integer> {
    List<nxvShipmentDetail> findByShipmentRequest(nxvShipmentRequest shipmentRequest);}