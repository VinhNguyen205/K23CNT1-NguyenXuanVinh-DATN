package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvOAuthProviders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvOAuthProviders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OAuthID")
    private Long oauthId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "Provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "ProviderUserID", nullable = false, length = 200)
    private String providerUserId;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}