package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "UserPityStats")
public class nxvUserPityStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StatID")
    private Integer statId;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private nxvUser user;

    @ManyToOne
    @JoinColumn(name = "BoxID")
    private nxvBlindBox blindBox;

    @Column(name = "SpinsWithoutS")
    private Integer spinsWithoutS;

    @Column(name = "LastSRewardDate")
    private LocalDateTime lastSRewardDate;
}