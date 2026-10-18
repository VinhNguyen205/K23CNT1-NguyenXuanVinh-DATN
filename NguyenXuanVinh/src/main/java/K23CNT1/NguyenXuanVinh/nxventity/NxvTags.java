package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvTags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TagID")
    private Integer tagId;

    @Column(name = "TagName", nullable = false, unique = true, length = 50)
    private String tagName;

    @Builder.Default
    @Column(name = "ColorHex", nullable = false, length = 7)
    private String colorHex = "#FFFFFF";
}