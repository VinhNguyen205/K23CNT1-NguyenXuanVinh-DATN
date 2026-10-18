package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserTitles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserTitles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserTitleID")
    private Long userTitleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TitleID", nullable = false)
    private NxvTitles title;

    @Builder.Default
    @Column(name = "UnlockedAt", nullable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();
}