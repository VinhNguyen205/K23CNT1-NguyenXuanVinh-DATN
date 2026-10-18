package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Users")
public class nxvUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "Username", nullable = false, unique = true)
    private String username;

    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Column(name = "Email")
    private String email;

    @Column(name = "FullName")
    private String fullName;

    @Column(name = "Address") // Mới thêm
    private String address;

    @Column(name = "PhoneNumber") // Mới thêm
    private String phoneNumber;

    @Column(name = "WalletBalance", precision = 18, scale = 2)
    private BigDecimal walletBalance;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "IsAdmin")
    private Boolean isAdmin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (walletBalance == null) walletBalance = BigDecimal.ZERO;
        if (isAdmin == null) isAdmin = false;
    }
}