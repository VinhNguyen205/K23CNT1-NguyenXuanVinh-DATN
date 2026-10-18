package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Banners")
public class nxvBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BannerID")
    private Integer bannerId;

    @Column(name = "Title")
    private String title;

    @Column(name = "ImageURL", nullable = false)
    private String imageUrl;

    @Column(name = "LinkUrl")
    private String linkUrl;

    @Column(name = "DisplayOrder")
    private Integer displayOrder;

    @Column(name = "IsActive")
    private Boolean isActive;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (displayOrder == null) displayOrder = 0;
    }
}