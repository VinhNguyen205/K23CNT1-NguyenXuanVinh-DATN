package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvPetDex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvPetDex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PetDexID")
    private Long petDexId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SpeciesID", nullable = false)
    private NxvPetSpecies species;

    @Builder.Default
    @Column(name = "FirstObtainedAt", nullable = false)
    private LocalDateTime firstObtainedAt = LocalDateTime.now();
}