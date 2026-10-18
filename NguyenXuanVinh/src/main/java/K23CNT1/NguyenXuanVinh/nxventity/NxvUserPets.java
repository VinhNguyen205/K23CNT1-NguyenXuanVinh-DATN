package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserPets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserPets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserPetID")
    private Long userPetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SpeciesID", nullable = false)
    private NxvPetSpecies species;

    @Builder.Default
    @Column(name = "PetName", nullable = false, length = 100)
    private String petName = "ThÃº CÆ°ng";

    @Column(name = "ElementType", length = 50)
    private String elementType;

    @Builder.Default
    @Column(name = "Stage", nullable = false)
    private Integer stage = 0;

    @Column(name = "CurrentImageUrl", length = 500)
    private String currentImageUrl;

    @Builder.Default
    @Column(name = "Hunger", nullable = false)
    private Integer hunger = 100;

    @Builder.Default
    @Column(name = "Affection", nullable = false)
    private Integer affection = 50;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = false;

    @Builder.Default
    @Column(name = "IsHibernating", nullable = false)
    private Boolean isHibernating = false;

    @Column(name = "HatchedAt")
    private LocalDateTime hatchedAt;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}