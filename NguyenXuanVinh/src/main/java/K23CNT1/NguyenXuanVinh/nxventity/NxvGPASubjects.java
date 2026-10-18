package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvGPASubjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvGPASubjects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubjectID")
    private Long subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpicQuestID", nullable = false)
    private NxvEpicQuests epicQuest;

    @Column(name = "SubjectName", nullable = false, length = 200)
    private String subjectName;

    @Builder.Default
    @Column(name = "Credits", nullable = false)
    private Integer credits = 3;

    @Column(name = "Score", precision = 4, scale = 2)
    private BigDecimal score;

    @Builder.Default
    @Column(name = "IsExcluded", nullable = false)
    private Boolean isExcluded = false;

    @Column(name = "ExcludeReason", length = 100)
    private String excludeReason;
}