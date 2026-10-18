package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserReports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserReports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReporterUserID", nullable = false)
    private Users reporterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetUserID")
    private Users targetUser;

    @Column(name = "ReportType", nullable = false, length = 50)
    private String reportType;

    @Column(name = "Description", nullable = false, length = 2000)
    private String description;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}