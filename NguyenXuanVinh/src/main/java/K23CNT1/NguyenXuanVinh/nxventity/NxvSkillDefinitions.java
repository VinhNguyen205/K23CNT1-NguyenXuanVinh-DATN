package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvSkillDefinitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvSkillDefinitions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SkillID")
    private Integer skillId;

    @Column(name = "SkillName", nullable = false, length = 100)
    private String skillName;

    @Builder.Default
    @Column(name = "SkillType", nullable = false)
    private Integer skillType = 0;

    @Builder.Default
    @Column(name = "MPCost", nullable = false)
    private Integer mpCost = 0;

    @Builder.Default
    @Column(name = "CooldownSeconds", nullable = false)
    private Integer cooldownSeconds = 0;

    @Builder.Default
    @Column(name = "RequiredLevel", nullable = false)
    private Integer requiredLevel = 1;

    @Builder.Default
    @Column(name = "MaxSkillLevel", nullable = false)
    private Integer maxSkillLevel = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentSkillID")
    private NxvSkillDefinitions parentSkill;

    @Column(name = "SkillEffect", length = 50)
    private String skillEffect;
}