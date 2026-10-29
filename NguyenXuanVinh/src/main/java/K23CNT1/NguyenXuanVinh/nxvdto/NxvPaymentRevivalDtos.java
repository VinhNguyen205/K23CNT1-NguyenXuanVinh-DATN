package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class NxvPaymentRevivalDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RevivalPackageDto {
        private Integer packageId;
        private String packageName;
        private Integer amountVnd;
        private String benefitDescription;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeathStateStatusDto {
        private Long userId;
        private String username;
        private String characterName;
        private Boolean isDead;
        private Integer currentHp;
        private Integer maxHp;
        private Integer currentMp;
        private Integer maxMp;
        private Boolean isInteractionLocked;
        private String lockMessage;
        private List<RevivalPackageDto> availablePackages;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRevivalOrderRequest {
        @NotNull(message = "packageId (Gói Vé Hồi Sinh) không được để trống")
        private Integer packageId;

        @Builder.Default
        private String paymentMethod = "VIETQR"; // VIETQR hoac MOMO
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaymentOrderQrResponse {
        private Long orderId;
        private Long userId;
        private String orderCode;
        private String packageName;
        private Integer amountVnd;
        private Integer status; // 0=Pending, 1=Paid, 2=Cancelled
        private String statusLabel;
        private String paymentMethod;
        private String bankBin;
        private String bankAccountNo;
        private String bankAccountName;
        private String transferContent;
        private String dynamicVietQrImageUrl;
        private String momoQrDeepLink;
        private String expectedWebhookSignature; // Giup copy test Webhook tren Swagger de dang
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BankWebhookPayload {
        @NotBlank(message = "orderCode không được để trống")
        private String orderCode;

        @NotNull(message = "amountVnd không được để trống")
        private Integer amountVnd;

        private String bankTransactionId;
        private String transferContent;

        @NotBlank(message = "Chữ ký bảo mật (signatureHash) không được để trống")
        private String signatureHash;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BillingInvoiceDto {
        private Long billingTxId;
        private Long orderId;
        private String orderCode;
        private Long userId;
        private String username;
        private String email;
        private String characterName;
        private Integer amountVnd;
        private LocalDateTime confirmedAt;
        private String electronicReceiptSummary;
    }
}