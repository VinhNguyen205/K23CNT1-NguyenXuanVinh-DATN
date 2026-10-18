package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvPetInteractionLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvPetInteractionLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserPetID", nullable = false)
    private NxvUserPets userPet;

    @Column(name = "InteractionType", nullable = false, length = 30)
    private String interactionType;

    @Column(name = "UsedItemID")
    private Integer usedItemId;

    @Column(name = "AIMessage", length = 1000)
    private String aiMessage;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}