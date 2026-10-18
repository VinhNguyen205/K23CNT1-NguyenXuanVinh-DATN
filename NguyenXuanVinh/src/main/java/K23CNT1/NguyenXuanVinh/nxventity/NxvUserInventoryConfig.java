package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserInventoryConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserInventoryConfig {
    @Id
    @Column(name = "UserID")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", insertable = false, updatable = false)
    private Users user;

    @Builder.Default
    @Column(name = "MaxSlots", nullable = false)
    private Integer maxSlots = 50;
}