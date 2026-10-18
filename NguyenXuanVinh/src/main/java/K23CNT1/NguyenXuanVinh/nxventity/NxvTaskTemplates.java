package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvTaskTemplates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvTaskTemplates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TemplateID")
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Builder.Default
    @Column(name = "Difficulty", nullable = false)
    private Integer difficulty = 1;

    @Column(name = "RecurringRule", nullable = false, length = 100)
    private String recurringRule;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}