package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvAIConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvAIConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConfigID")
    private Integer configId;

    @Column(name = "Provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "ModelName", nullable = false, length = 100)
    private String modelName;

    @Column(name = "APIKeyEncrypted", nullable = false, length = 1000)
    private String apiKeyEncrypted;

    @Column(name = "SystemPrompt", columnDefinition = "NVARCHAR(MAX)")
    private String systemPrompt;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}