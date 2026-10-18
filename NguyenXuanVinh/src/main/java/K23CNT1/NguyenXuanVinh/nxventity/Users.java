package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "PasswordHash", length = 256)
    private String passwordHash;

    @Builder.Default
    @Column(name = "Gender", nullable = false)
    private Integer gender = 0;

    @Column(name = "DefaultAvatarUrl", length = 500)
    private String defaultAvatarUrl;

    @Builder.Default
    @Column(name = "Role", nullable = false)
    private Integer role = 0;

    @Builder.Default
    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean isEmailVerified = false;

    @Builder.Default
    @Column(name = "IsLocked", nullable = false)
    private Boolean isLocked = false;

    @Builder.Default
    @Column(name = "IsDead", nullable = false)
    private Boolean isDead = false;

    @Builder.Default
    @Column(name = "LoginFailCount", nullable = false)
    private Integer loginFailCount = 0;

    @Column(name = "LastLoginAt")
    private LocalDateTime lastLoginAt;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;
}