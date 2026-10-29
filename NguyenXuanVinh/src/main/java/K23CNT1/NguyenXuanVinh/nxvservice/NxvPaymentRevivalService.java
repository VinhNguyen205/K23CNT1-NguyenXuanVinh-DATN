package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvPaymentRevivalDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
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

    @Value("${nxv.payment.webhook-secret:TaskTaleVietQrWebhookSecretKey2026NguyenXuanVinh}")
    private String webhookSecretKey;

    @Value("${nxv.payment.bank-id:MB}")
    private String bankId;

    @Value("${nxv.payment.account-no:0389999999}")
    private String bankAccountNo;

    @Value("${nxv.payment.account-name:NGUYEN XUAN VINH TASKTALE}")
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