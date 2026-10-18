package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvNotifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvNotifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private Users user;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Content", nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @Column(name = "NotificationType", nullable = false, length = 30)
    private String notificationType = "SYSTEM";

    @Builder.Default
    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}