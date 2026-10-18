package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ShipmentDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nxvShipmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DetailID")
    private Integer detailId;

    @ManyToOne
    @JoinColumn(name = "ShipmentID")
    private nxvShipmentRequest shipmentRequest;

    @ManyToOne
    @JoinColumn(name = "InventoryID")
    private nxvUserInventory userInventory;
}