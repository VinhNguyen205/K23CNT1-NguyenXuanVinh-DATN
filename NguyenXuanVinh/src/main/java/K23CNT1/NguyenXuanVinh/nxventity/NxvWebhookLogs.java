package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvWebhookLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvWebhookLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WebhookLogID")
    private Long webhookLogId;

    @Column(name = "OrderCode", nullable = false, length = 50)
    private String orderCode;

    @Column(name = "PayloadJson", columnDefinition = "NVARCHAR(MAX)")
    private String payloadJson;

    @Column(name = "SignatureHash", length = 256)
    private String signatureHash;

    @Builder.Default
    @Column(name = "ProcessStatus", nullable = false)
    private Integer processStatus = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}