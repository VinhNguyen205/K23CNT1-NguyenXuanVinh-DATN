package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ShipmentRequests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nxvShipmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShipmentID")
    private Integer shipmentId;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private nxvUser user;

    @Column(name = "ReceiverName")
    private String receiverName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Address")
    private String address;

    @Column(name = "Note")
    private String note;

    @Column(name = "RequestDate")
    private LocalDateTime requestDate;

    @Column(name = "ShipmentStatus")
    private String shipmentStatus;

    // [QUAN TRỌNG] Đây là trường mà trang HTML đang tìm kiếm (req.shipmentDetails)
    @OneToMany(mappedBy = "shipmentRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<nxvShipmentDetail> shipmentDetails;

    @PrePersist
    public void onCreate() {
        if (this.requestDate == null) this.requestDate = LocalDateTime.now();
        if (this.shipmentStatus == null) this.shipmentStatus = "PENDING";
    }
}