package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvEquipmentSlots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvEquipmentSlots {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SlotID")
    private Integer slotId;

    @Column(name = "SlotName", nullable = false, length = 50)
    private String slotName;

    @Column(name = "SlotKey", nullable = false, unique = true, length = 30)
    private String slotKey;
}