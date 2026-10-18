package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CartItems")
public class nxvCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartItemID")
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name = "CartID")
    private nxvCart cart;

    @ManyToOne
    @JoinColumn(name = "ProductID") // Mua sản phẩm thường (có thể null)
    private nxvProduct product;

    @ManyToOne
    @JoinColumn(name = "BoxID") // Mua Blind Box (có thể null)
    private nxvBlindBox blindBox;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "AddedAt")
    private LocalDateTime addedAt;
}