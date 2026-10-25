# ==============================================================================
# TASKTALE / LIFERPG - TASK 8 AUTOMATED GENERATOR (MODULE 9: PAYMENT & REVIVAL)
# Hoan thien Chuc nang #116 -> #122: Death State Lock, Revival Packages,
# Dynamic VietQR/Momo Order Generation, HMAC-SHA256 Checksum Signature Verification,
# Bank Webhook/IPN Listener, Instant Character Revival, Billing & Email Receipt
# ==============================================================================

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Write-JavaFile {
    param([string]$FilePath, [string]$Content)
    $dir = Split-Path -Parent $FilePath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    if (Test-Path $FilePath) {
        Remove-Item $FilePath -Force
    }
    [System.IO.File]::WriteAllText($FilePath, $Content, $utf8NoBom)
}

$javaRoot = Join-Path (Get-Location) "src\main\java"
$entityDirObj = Get-ChildItem -Path $javaRoot -Recurse -Directory -Filter "nxventity" | Select-Object -First 1
$baseDir = $entityDirObj.Parent.FullName
$basePkg = $baseDir.Substring($javaRoot.Length).TrimStart('\', '/') -replace '[\\/]', '.'

Write-Host "==> Dang sinh Module 9 (Task 8: VietQR Payment, HMAC Webhook, Revival & Billing) vao: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 9
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvPaymentOrdersRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvPaymentOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvPaymentOrdersRepository extends JpaRepository<NxvPaymentOrders, Long> {
    Optional<NxvPaymentOrders> findByOrderCode(String orderCode);
    List<NxvPaymentOrders> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<NxvPaymentOrders> findTop100ByOrderByCreatedAtDesc();
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvWebhookLogsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvWebhookLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvWebhookLogsRepository extends JpaRepository<NxvWebhookLogs, Long> {
    List<NxvWebhookLogs> findTop100ByOrderByCreatedAtDesc();
    List<NxvWebhookLogs> findByOrderCodeOrderByCreatedAtDesc(String orderCode);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvBillingTransactionsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvBillingTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvBillingTransactionsRepository extends JpaRepository<NxvBillingTransactions, Long> {
    List<NxvBillingTransactions> findByUser_UserIdOrderByConfirmedAtDesc(Long userId);
    List<NxvBillingTransactions> findTop100ByOrderByConfirmedAtDesc();
    Optional<NxvBillingTransactions> findByOrder_OrderId(Long orderId);
}
"@

# ==============================================================================
# 2. SINH DTO CHO MODULE 9 (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvPaymentRevivalDtos.java" @"
package $basePkg.nxvdto;

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
"@

# ==============================================================================
# 3. SINH SERVICE MODULE 9 (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvPaymentRevivalService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvPaymentRevivalDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NxvPaymentRevivalService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvRevivalPackagesRepository revivalPackagesRepository;
    private final NxvPaymentOrdersRepository paymentOrdersRepository;
    private final NxvWebhookLogsRepository webhookLogsRepository;
    private final NxvBillingTransactionsRepository billingTransactionsRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("`${nxv.payment.webhook-secret:TaskTaleVietQrWebhookSecretKey2026NguyenXuanVinh}")
    private String webhookSecretKey;

    @Value("`${nxv.payment.bank-id:MB}")
    private String bankId;

    @Value("`${nxv.payment.account-no:0389999999}")
    private String bankAccountNo;

    @Value("`${nxv.payment.account-name:NGUYEN XUAN VINH TASKTALE}")
    private String bankAccountName;

    // =========================================================================
    // PHAN 1: TRANG THAI TU VONG (DEATH STATE) & GOI VE HOI SINH (#116, #117)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<RevivalPackageDto> getAllRevivalPackages() {
        return revivalPackagesRepository.findAll().stream()
                .map(p -> RevivalPackageDto.builder()
                        .packageId(p.getPackageId())
                        .packageName(p.getPackageName())
                        .amountVnd(p.getAmountVnd())
                        .benefitDescription(p.getAmountVnd() >= 50000
                                ? "Hồi sinh nhân vật 100% HP & MP + Tặng kèm 500 Gold + Bật sẵn Khiên Chắn bảo vệ!"
                                : "Hồi sinh nhân vật ngay lập tức với 100% HP & MP, mở khóa toàn bộ tính năng!")
                        .build())
                .collect(Collectors.toList());
    }

    // Chuc nang #116: Kiem tra Trang thai Tu vong (Death State) & Khoa tuong tac
    @Transactional(readOnly = true)
    public DeathStateStatusDto getDeathStateStatus(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));

        boolean dead = Boolean.TRUE.equals(u.getIsDead()) || c.getCurrentHp() <= 0;
        String msg = dead
                ? "NHÂN VẬT ĐÃ TỬ VONG (HP = 0)! Toàn bộ tính năng nhận Task, điểm danh, Gacha và Buff Pet đã bị khóa. Vui lòng quét mã QR mua Vé Hồi Sinh để tiếp tục!"
                : "Nhân vật đang sống khỏe mạnh (" + c.getCurrentHp() + "/" + c.getMaxHp() + " HP). Mọi tính năng hoạt động bình thường.";

        return DeathStateStatusDto.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .characterName(c.getCharacterName())
                .isDead(dead)
                .currentHp(c.getCurrentHp())
                .maxHp(c.getMaxHp())
                .currentMp(c.getCurrentMp())
                .maxMp(c.getMaxMp())
                .isInteractionLocked(dead)
                .lockMessage(msg)
                .availablePackages(getAllRevivalPackages())
                .build();
    }

    // API ho tro Demo Bao ve Do an: Gia lap nhan vat het mau (HP = 0, IsDead = true)
    @Transactional
    public DeathStateStatusDto simulateCharacterDeathForDemo(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));

        c.setCurrentHp(0);
        c.setUpdatedAt(LocalDateTime.now());
        charactersRepository.save(c);

        u.setIsDead(true);
        u.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(u);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(u)
                .logType("HP")
                .delta(0L)
                .sourceType("DeathStateTriggered")
                .createdAt(LocalDateTime.now())
                .build());

        notificationsRepository.save(NxvNotifications.builder()
                .user(u)
                .title("CẢNH BÁO: Nhân vật đã gục ngã (HP = 0)!")
                .content("Nhân vật " + c.getCharacterName() + " đã cạn sạch máu! Mọi tương tác đã bị khóa. Hãy mua Vé Hồi Sinh qua VietQR/Momo để trở lại cuộc chơi.")
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return getDeathStateStatus(userId);
    }

    // =========================================================================
    // PHAN 2: TAO DON HANG & MA QR DONG VIETQR / MOMO (#117, #118)
    // =========================================================================

    // Chuc nang #117 & #118: Tao don thanh toan & Sinh ma QR Dong chua so tien + noi dung theo UserID
    @Transactional
    public PaymentOrderQrResponse createRevivalPaymentOrder(Long userId, CreateRevivalOrderRequest req) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        NxvRevivalPackages pkg = revivalPackagesRepository.findById(req.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Gói Vé Hồi Sinh ID: " + req.getPackageId()));

        String orderCode = "TTREVIVEU" + userId + "T" + (System.currentTimeMillis() % 100000000L);
        NxvPaymentOrders order = paymentOrdersRepository.save(NxvPaymentOrders.builder()
                .user(u)
                .orderCode(orderCode)
                .amountVnd(pkg.getAmountVnd())
                .status(0) // 0 = Pending
                .createdAt(LocalDateTime.now())
                .build());

        return buildOrderQrResponse(order, pkg.getPackageName(), req.getPaymentMethod());
    }

    @Transactional(readOnly = true)
    public List<PaymentOrderQrResponse> getMyPaymentOrders(Long userId) {
        return paymentOrdersRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> buildOrderQrResponse(o, o.getAmountVnd() >= 50000 ? "Vé Hồi Sinh Hoàng Kim" : "Vé Hồi Sinh Cơ Bản", "VIETQR"))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // PHAN 3: WEBHOOK/IPN LISTENER, CHU KY HMAC-SHA256, HOI SINH & EMAIL (#119 -> #122)
    // =========================================================================

    // Chuc nang #120: Ma hoa chu ky HMAC-SHA256 chong gia mao Webhook
    public String computeHmacSha256Signature(String orderCode, Integer amountVnd) {
        try {
            String rawData = "orderCode=" + orderCode.trim() + "&amountVnd=" + amountVnd;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hashBytes = sha256Hmac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi tính toán chữ ký bảo mật HMAC-SHA256: " + e.getMessage());
        }
    }

    // Chuc nang #119, #120, #121, #122: Xu ly Webhook tu Ngan hang -> Xac thuc Checksum -> Mo khoa Hoi sinh -> Luu Billing & Gui Email
    @Transactional
    public Map<String, Object> processBankPaymentWebhook(BankWebhookPayload payload) {
        String orderCode = payload.getOrderCode().trim();
        String expectedSignature = computeHmacSha256Signature(orderCode, payload.getAmountVnd());

        boolean signatureValid = expectedSignature.equalsIgnoreCase(payload.getSignatureHash().trim());
        String rawPayloadJson = String.format(
                "{\"orderCode\":\"%s\",\"amountVnd\":%d,\"bankTxId\":\"%s\",\"transferContent\":\"%s\"}",
                orderCode, payload.getAmountVnd(),
                payload.getBankTransactionId() != null ? payload.getBankTransactionId() : "BANK_IPN",
                payload.getTransferContent() != null ? payload.getTransferContent() : orderCode
        );

        // Luu NxvWebhookLogs de audit (#119)
        NxvWebhookLogs webhookLog = webhookLogsRepository.save(NxvWebhookLogs.builder()
                .orderCode(orderCode)
                .payloadJson(rawPayloadJson)
                .signatureHash(payload.getSignatureHash())
                .processStatus(signatureValid ? 1 : 2) // 1=ValidProcessed, 2=InvalidSignature
                .createdAt(LocalDateTime.now())
                .build());

        if (!signatureValid) {
            throw new AppException(ErrorCode.FORBIDDEN,
                    "CẢNH BÁO BẢO MẬT: Chữ ký Checksum/Signature của Webhook không hợp lệ! Giao dịch bị từ chối.");
        }

        NxvPaymentOrders order = paymentOrdersRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy đơn thanh toán với mã: " + orderCode));

        if (order.getStatus() == 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng này đã được thanh toán và hồi sinh nhân vật trước đó rồi!");
        }
        if (payload.getAmountVnd() < order.getAmountVnd()) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Số tiền chuyển khoản (" + payload.getAmountVnd() + "đ) nhỏ hơn giá trị đơn hàng (" + order.getAmountVnd() + "đ)!");
        }

        // 1. Cap nhat don hang sang PAID (Status = 1)
        order.setStatus(1);
        paymentOrdersRepository.save(order);

        // 2. Luu Hoa don tien that vao NxvBillingTransactions (#121)
        Users user = order.getUser();
        LocalDateTime now = LocalDateTime.now();
        NxvBillingTransactions billingTx = billingTransactionsRepository.save(NxvBillingTransactions.builder()
                .order(order)
                .user(user)
                .amountVnd(payload.getAmountVnd())
                .confirmedAt(now)
                .build());

        // 3. Mo khoa Trang thai Tu vong & Hoi sinh Nhan vat ngay lap tuc (#119)
        user.setIsDead(false);
        user.setUpdatedAt(now);
        usersRepository.save(user);

        NxvCharacters c = charactersRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));

        c.setCurrentHp(c.getMaxHp());
        c.setCurrentMp(c.getMaxMp());

        long bonusGold = 0L;
        if (payload.getAmountVnd() >= 50000) {
            // Goi Hoang Kim: Tang them 500 Gold va bat san Khien Chan bao ve
            bonusGold = 500L;
            c.setGold(c.getGold() + bonusGold);
            c.setIsShieldActive(true);

            statLogsRepository.save(NxvStatLogs.builder()
                    .user(user)
                    .logType("Gold")
                    .delta(bonusGold)
                    .sourceType("GoldenRevivalPackage")
                    .sourceId(order.getOrderId())
                    .createdAt(now)
                    .build());
        }

        c.setUpdatedAt(now);
        charactersRepository.save(c);

        statLogsRepository.save(NxvStatLogs.builder()
                .user(user)
                .logType("HP")
                .delta(c.getMaxHp().longValue())
                .sourceType("RevivalWebhook")
                .sourceId(order.getOrderId())
                .createdAt(now)
                .build());

        // 4. Gui Email Bien lai dien tu & Thong bao he thong (#122)
        boolean emailSent = sendRevivalReceiptEmail(user, c, order, billingTx, bonusGold);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("webhookLogId", webhookLog.getWebhookLogId());
        res.put("billingTxId", billingTx.getBillingTxId());
        res.put("orderCode", order.getOrderCode());
        res.put("amountVnd", billingTx.getAmountVnd());
        res.put("userId", user.getUserId());
        res.put("characterName", c.getCharacterName());
        res.put("isDead", user.getIsDead());
        res.put("restoredHp", c.getCurrentHp());
        res.put("restoredMp", c.getCurrentMp());
        res.put("bonusGoldGranted", bonusGold);
        res.put("emailReceiptSent", emailSent);
        res.put("message", "Thanh toán thành công! Nhân vật [" + c.getCharacterName() + "] đã được hồi sinh 100% HP/MP!");
        return res;
    }

    // API ho tro Demo 1-Click tren Swagger: Tu dong tao chu ky HMAC chuan va goi Webhook hoi sinh luon
    @Transactional
    public Map<String, Object> simulateInstantWebhookSuccessForOrder(String orderCode) {
        NxvPaymentOrders order = paymentOrdersRepository.findByOrderCode(orderCode.trim())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy đơn hàng với mã: " + orderCode));

        String validSig = computeHmacSha256Signature(order.getOrderCode(), order.getAmountVnd());
        BankWebhookPayload simulatedPayload = BankWebhookPayload.builder()
                .orderCode(order.getOrderCode())
                .amountVnd(order.getAmountVnd())
                .bankTransactionId("VIETQR_DEMO_" + System.currentTimeMillis())
                .transferContent("HOI SINH USER " + order.getUser().getUserId() + " " + order.getOrderCode())
                .signatureHash(validSig)
                .build();

        return processBankPaymentWebhook(simulatedPayload);
    }

    // Chuc nang #121: Xem danh sach Hoa don (Billing) de doi soat
    @Transactional(readOnly = true)
    public List<BillingInvoiceDto> getMyBillingInvoices(Long userId) {
        return billingTransactionsRepository.findByUser_UserIdOrderByConfirmedAtDesc(userId).stream()
                .map(this::toBillingInvoiceDto)
                .collect(Collectors.toList());
    }

    // --- Helpers ---
    private PaymentOrderQrResponse buildOrderQrResponse(NxvPaymentOrders order, String pkgName, String method) {
        Long uid = order.getUser().getUserId();
        String transferInfo = "HOISINH U" + uid + " " + order.getOrderCode();
        String encodedInfo = URLEncoder.encode(transferInfo, StandardCharsets.UTF_8);
        String encodedAccName = URLEncoder.encode(bankAccountName, StandardCharsets.UTF_8);

        // Link anh QR Dong chuan VietQR.io chua san so tien va noi dung chuyen khoan (#118)
        String vietQrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bankId, bankAccountNo, order.getAmountVnd(), encodedInfo, encodedAccName
        );
        String momoLink = String.format(
                "https://nhantien.momo.vn/%s/%d?note=%s",
                bankAccountNo, order.getAmountVnd(), encodedInfo
        );

        String stLabel = switch (order.getStatus() != null ? order.getStatus() : 0) {
            case 1 -> "PAID (Đã thanh toán & Hồi sinh)";
            case 2 -> "CANCELLED (Đã hủy)";
            default -> "PENDING (Đang chờ quét mã QR)";
        };

        String sig = computeHmacSha256Signature(order.getOrderCode(), order.getAmountVnd());

        return PaymentOrderQrResponse.builder()
                .orderId(order.getOrderId())
                .userId(uid)
                .orderCode(order.getOrderCode())
                .packageName(pkgName)
                .amountVnd(order.getAmountVnd())
                .status(order.getStatus())
                .statusLabel(stLabel)
                .paymentMethod(method != null ? method.toUpperCase() : "VIETQR")
                .bankBin(bankId)
                .bankAccountNo(bankAccountNo)
                .bankAccountName(bankAccountName)
                .transferContent(transferInfo)
                .dynamicVietQrImageUrl(vietQrUrl)
                .momoQrDeepLink(momoLink)
                .expectedWebhookSignature(sig)
                .createdAt(order.getCreatedAt())
                .build();
    }

    // Chuc nang #122: Gui Email Bien lai hoi sinh kem hoa don dien tu
    private boolean sendRevivalReceiptEmail(Users user,
                                            NxvCharacters c,
                                            NxvPaymentOrders order,
                                            NxvBillingTransactions tx,
                                            long bonusGold) {
        String subject = "[TaskTale LifeRPG] Biên lai Hồi sinh Nhân vật - Mã HĐ #" + tx.getBillingTxId();
        String body = "Xin chào " + user.getUsername() + ",\n\n"
                + "Hệ thống TaskTale / LifeRPG xác nhận đã nhận thanh toán mua Vé Hồi Sinh thành công!\n"
                + "--------------------------------------------------\n"
                + "- Mã Hóa đơn (Billing ID): #" + tx.getBillingTxId() + "\n"
                + "- Mã Đơn hàng (Order Code): " + order.getOrderCode() + "\n"
                + "- Số tiền thanh toán: " + String.format("%,d", tx.getAmountVnd()) + " VNĐ\n"
                + "- Nhân vật được hồi sinh: " + c.getCharacterName() + " (HP: " + c.getCurrentHp() + "/" + c.getMaxHp() + ")\n"
                + (bonusGold > 0 ? "- Quà tặng Gói Hoàng Kim: +" + bonusGold + " Gold & Bật Khiên Chắn\n" : "")
                + "- Thời gian xác nhận: " + tx.getConfirmedAt() + "\n"
                + "--------------------------------------------------\n"
                + "Chúc bạn giữ vững kỷ luật và không bao giờ để nhân vật gục ngã nữa nhé!";

        notificationsRepository.save(NxvNotifications.builder()
                .user(user)
                .title(subject)
                .content(body)
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        if (mailSender != null) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(user.getEmail());
                mail.setSubject(subject);
                mail.setText(body);
                mailSender.send(mail);
                return true;
            } catch (Exception ex) {
                log.warn("[TaskTale Email] Khong the gui SMTP that, da luu Bien lai dien tu vao NxvNotifications: {}", ex.getMessage());
            }
        }
        return false;
    }

    public BillingInvoiceDto toBillingInvoiceDto(NxvBillingTransactions tx) {
        Users u = tx.getUser();
        NxvCharacters c = charactersRepository.findByUser_UserId(u.getUserId()).orElse(null);
        String charName = (c != null) ? c.getCharacterName() : u.getUsername();

        return BillingInvoiceDto.builder()
                .billingTxId(tx.getBillingTxId())
                .orderId(tx.getOrder().getOrderId())
                .orderCode(tx.getOrder().getOrderCode())
                .userId(u.getUserId())
                .username(u.getUsername())
                .email(u.getEmail())
                .characterName(charName)
                .amountVnd(tx.getAmountVnd())
                .confirmedAt(tx.getConfirmedAt())
                .electronicReceiptSummary("HĐ #" + tx.getBillingTxId() + " | Đơn " + tx.getOrder().getOrderCode()
                        + " | " + String.format("%,d", tx.getAmountVnd()) + " VNĐ | Hồi sinh nhân vật: " + charName)
                .build();
    }
}
"@

# ==============================================================================
# 4. SINH REST CONTROLLER CHO MODULE 9 (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvPaymentRevivalController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvPaymentRevivalDtos.*;
import $basePkg.nxvservice.NxvPaymentRevivalService;
import $basePkg.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment-revival")
@RequiredArgsConstructor
@Tag(name = "Module 9: Cổng Thanh toán VietQR/Momo & Trạng thái Sinh tử (Chức năng #116 - #122)", description = "Trạng thái Tử vong (Death State), Sinh QR Động VietQR/Momo, Webhook HMAC-SHA256, Hồi sinh Nhân vật, Hóa đơn Billing & Gửi Email Biên lai")
public class NxvPaymentRevivalController {

    private final NxvPaymentRevivalService paymentRevivalService;

    @Operation(summary = "[#116] Kiểm tra Trạng thái Sinh tử (Death State) và danh sách Gói Vé Hồi Sinh")
    @GetMapping("/death-state/me")
    public ApiResponse<DeathStateStatusDto> getMyDeathState() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(paymentRevivalService.getDeathStateStatus(userId));
    }

    @Operation(summary = "[#116-DEMO] Giả lập Nhân vật hết máu (HP = 0 -> IsDead = true) để test khóa tương tác & Hồi sinh khi bảo vệ đồ án")
    @PostMapping("/death-state/simulate-death")
    public ApiResponse<DeathStateStatusDto> simulateDeath() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã kích hoạt trạng thái Tử vong (HP = 0, IsDead = true)!", paymentRevivalService.simulateCharacterDeathForDemo(userId));
    }

    @Operation(summary = "[#117] Xem danh sách các Gói Vé Hồi Sinh (Vé Cơ Bản 20.000đ / Vé Hoàng Kim 50.000đ)")
    @GetMapping("/packages")
    public ApiResponse<List<RevivalPackageDto>> getPackages() {
        return ApiResponse.ok(paymentRevivalService.getAllRevivalPackages());
    }

    @Operation(summary = "[#117, #118] Tạo đơn mua Vé Hồi Sinh & Sinh mã QR Động (VietQR / Momo) chứa sẵn số tiền và nội dung chuyển khoản theo UserID")
    @PostMapping("/orders")
    public ApiResponse<PaymentOrderQrResponse> createRevivalOrder(@Valid @RequestBody CreateRevivalOrderRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã sinh đơn hàng và mã QR Động VietQR/Momo thành công!", paymentRevivalService.createRevivalPaymentOrder(userId, request));
    }

    @Operation(summary = "[#118B] Xem lịch sử các đơn đặt mua Vé Hồi Sinh của tôi")
    @GetMapping("/orders/me")
    public ApiResponse<List<PaymentOrderQrResponse>> getMyOrders() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(paymentRevivalService.getMyPaymentOrders(userId));
    }

    @Operation(summary = "[#119, #120, #122] Webhook/IPN Listener: Nhận tín hiệu từ Ngân hàng, kiểm tra chữ ký HMAC-SHA256, mở khóa Hồi sinh ngay lập tức & gửi Email biên lai")
    @PostMapping("/webhook/bank-ipn")
    public ApiResponse<Map<String, Object>> handleBankWebhook(@Valid @RequestBody BankWebhookPayload payload) {
        return ApiResponse.ok("Xác thực Webhook thành công! Nhân vật đã được hồi sinh.", paymentRevivalService.processBankPaymentWebhook(payload));
    }

    @Operation(summary = "[#119-DEMO] Nút bấm Giả lập Ngân hàng báo tiền về thành công (Tự ký HMAC-SHA256 & kích hoạt Webhook hồi sinh ngay cho orderCode)")
    @PostMapping("/webhook/simulate-paid/{orderCode}")
    public ApiResponse<Map<String, Object>> simulatePaidWebhook(@PathVariable String orderCode) {
        return ApiResponse.ok("Giả lập tín hiệu chuyển khoản thành công! Nhân vật đã hồi sinh.", paymentRevivalService.simulateInstantWebhookSuccessForOrder(orderCode));
    }

    @Operation(summary = "[#121, #122] Xem danh sách Hóa đơn điện tử (Billing Transactions) của tôi")
    @GetMapping("/billing/me")
    public ApiResponse<List<BillingInvoiceDto>> getMyBillingInvoices() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(paymentRevivalService.getMyBillingInvoices(userId));
    }
}
"@

Write-Host "==> HOAN TAT SINH MODULE 9 (TASK 8: VIETQR DYNAMIC QR, HMAC WEBHOOK, REVIVAL & BILLING) THANH CONG!" -ForegroundColor Green