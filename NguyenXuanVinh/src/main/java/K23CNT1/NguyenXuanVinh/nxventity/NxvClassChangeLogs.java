package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvClassChangeLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvClassChangeLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FromClassID")
    private NxvClasses fromClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ToClassID", nullable = false)
    private NxvClasses toClass;

    @Builder.Default
    @Column(name = "ChangedAt", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
}