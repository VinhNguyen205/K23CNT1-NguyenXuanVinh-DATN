package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvPartyMessages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvPartyMessages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageID")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyID", nullable = false)
    private NxvParties party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SenderUserID", nullable = false)
    private Users senderUser;

    @Column(name = "Content", nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}