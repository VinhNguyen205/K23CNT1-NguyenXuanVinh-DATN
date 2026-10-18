package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvPartyMembers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvPartyMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PartyMemberID")
    private Long partyMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyID", nullable = false)
    private NxvParties party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Builder.Default
    @Column(name = "Role", nullable = false)
    private Integer role = 0;

    @Builder.Default
    @Column(name = "JoinedAt", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
}