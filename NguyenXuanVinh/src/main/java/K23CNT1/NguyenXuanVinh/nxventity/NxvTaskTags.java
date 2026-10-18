package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvTaskTags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvTaskTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TaskTagID")
    private Long taskTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TaskID", nullable = false)
    private NxvTasks task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TagID", nullable = false)
    private NxvTags tag;
}