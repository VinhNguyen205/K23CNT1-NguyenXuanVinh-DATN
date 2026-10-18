package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvUserSkills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvUserSkills {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserSkillID")
    private Long userSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SkillID", nullable = false)
    private NxvSkillDefinitions skill;

    @Builder.Default
    @Column(name = "IsUnlocked", nullable = false)
    private Boolean isUnlocked = false;

    @Builder.Default
    @Column(name = "SkillLevel", nullable = false)
    private Integer skillLevel = 1;

    @Column(name = "CooldownEndsAt")
    private LocalDateTime cooldownEndsAt;
}