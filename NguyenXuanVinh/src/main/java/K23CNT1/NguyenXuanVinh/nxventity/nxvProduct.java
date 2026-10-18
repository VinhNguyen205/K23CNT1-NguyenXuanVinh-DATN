package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Products")
public class nxvProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private nxvCategory category;

    @Column(name = "ProductName")
    private String productName;

    @Column(name = "Description")
    private String description;

    @Column(name = "Price", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "StockQuantity")
    private Integer stockQuantity;

    @Column(name = "ImageURL")
    private String imageUrl;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}