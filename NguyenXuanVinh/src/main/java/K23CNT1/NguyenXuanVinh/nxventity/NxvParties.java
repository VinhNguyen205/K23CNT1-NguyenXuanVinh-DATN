package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvParties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvParties {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PartyID")
    private Long partyId;

    @Column(name = "PartyName", nullable = false, length = 100)
    private String partyName;

    @Column(name = "Description", length = 300)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LeaderUserID", nullable = false)
    private Users leaderUser;

    @Builder.Default
    @Column(name = "MaxMembers", nullable = false)
    private Integer maxMembers = 5;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}