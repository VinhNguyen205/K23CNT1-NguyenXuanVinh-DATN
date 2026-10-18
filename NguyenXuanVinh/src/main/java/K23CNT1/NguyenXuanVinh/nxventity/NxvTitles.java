package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvTitles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvTitles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TitleID")
    private Integer titleId;

    @Column(name = "TitleName", nullable = false, length = 100)
    private String titleName;

    @Column(name = "TitleIcon", length = 500)
    private String titleIcon;

    @Builder.Default
    @Column(name = "IsExclusive", nullable = false)
    private Boolean isExclusive = false;
}