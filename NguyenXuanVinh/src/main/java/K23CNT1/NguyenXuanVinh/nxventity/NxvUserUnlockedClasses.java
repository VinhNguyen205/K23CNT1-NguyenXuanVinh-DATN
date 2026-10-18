package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserUnlockedClasses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserUnlockedClasses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UnlockID")
    private Long unlockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassID", nullable = false)
    private NxvClasses nxvClass;

    @Builder.Default
    @Column(name = "UnlockedAt", nullable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();
}