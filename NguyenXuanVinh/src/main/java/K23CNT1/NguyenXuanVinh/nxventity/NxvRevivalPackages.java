package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvRevivalPackages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvRevivalPackages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PackageID")
    private Integer packageId;

    @Column(name = "PackageName", nullable = false, length = 100)
    private String packageName;

    @Column(name = "AmountVND", nullable = false)
    private Integer amountVnd;
}