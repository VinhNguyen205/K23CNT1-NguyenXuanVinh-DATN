package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "OrderDetails")
public class nxvOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderDetailID")
    private Integer orderDetailId;

    @ManyToOne
    @JoinColumn(name = "OrderID")
    private nxvOrder order;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    private nxvProduct product;

    @ManyToOne
    @JoinColumn(name = "BoxID")
    private nxvBlindBox blindBox;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "PriceAtTime", precision = 18, scale = 2)
    private BigDecimal priceAtTime;
}