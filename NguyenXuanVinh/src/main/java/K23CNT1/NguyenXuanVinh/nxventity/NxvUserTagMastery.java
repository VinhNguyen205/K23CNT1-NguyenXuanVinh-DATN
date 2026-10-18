package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserTagMastery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserTagMastery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MasteryID")
    private Long masteryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TagID", nullable = false)
    private NxvTags tag;

    @Builder.Default
    @Column(name = "TaskCount", nullable = false)
    private Integer taskCount = 0;

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}