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
                                ? "Há»“i sinh nhÃ¢n váº­t 100% HP & MP + Táº·ng kÃ¨m 500 Gold + Báº­t sáºµn KhiÃªn Cháº¯n báº£o vá»‡!"
                                : "Há»“i sinh nhÃ¢n váº­t ngay láº­p tá»©c vá»›i 100% HP & MP, má»Ÿ khÃ³a toÃ n bá»™ tÃ­nh nÄƒng!")
                        .build())
                .collect(Collectors.toList());
    }

    // Chuc nang #116: Kiem tra Trang thai Tu vong (Death State) & Khoa tuong tac
    @Transactional(readOnly = true)
    public DeathStateStatusDto getDeathStateStatus(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n váº­t!"));

        boolean dead = Boolean.TRUE.equals(u.getIsDead()) || c.getCurrentHp() <= 0;
        String msg = dead
                ? "NHÃ‚N Váº¬T ÄÃƒ Tá»¬ VONG (HP = 0)! ToÃ n bá»™ tÃ­nh nÄƒng nháº­n Task, Ä‘iá»ƒm danh, Gacha vÃ  Buff Pet Ä‘Ã£ bá»‹ khÃ³a. Vui lÃ²ng quÃ©t mÃ£ QR mua VÃ© Há»“i Sinh Ä‘á»ƒ tiáº¿p tá»¥c!"
                : "NhÃ¢n váº­t Ä‘ang sá»‘ng khá»e máº¡nh (" + c.getCurrentHp() + "/" + c.getMaxHp() + " HP). Má»i tÃ­nh nÄƒng hoáº¡t Ä‘á»™ng bÃ¬nh thÆ°á»ng.";

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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        NxvCharacters c = charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n váº­t!"));

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
                .title("Cáº¢NH BÃO: NhÃ¢n váº­t Ä‘Ã£ gá»¥c ngÃ£ (HP = 0)!")
                .content("NhÃ¢n váº­t " + c.getCharacterName() + " Ä‘Ã£ cáº¡n sáº¡ch mÃ¡u! Má»i tÆ°Æ¡ng tÃ¡c Ä‘Ã£ bá»‹ khÃ³a. HÃ£y mua VÃ© Há»“i Sinh qua VietQR/Momo Ä‘á»ƒ trá»Ÿ láº¡i cuá»™c chÆ¡i.")
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        NxvRevivalPackages pkg = revivalPackagesRepository.findById(req.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y GÃ³i VÃ© Há»“i Sinh ID: " + req.getPackageId()));

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
                .map(o -> buildOrderQrResponse(o, o.getAmountVnd() >= 50000 ? "VÃ© Há»“i Sinh HoÃ ng Kim" : "VÃ© Há»“i Sinh CÆ¡ Báº£n", "VIETQR"))
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
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lá»—i tÃ­nh toÃ¡n chá»¯ kÃ½ báº£o máº­t HMAC-SHA256: " + e.getMessage());
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
                    "Cáº¢NH BÃO Báº¢O Máº¬T: Chá»¯ kÃ½ Checksum/Signature cá»§a Webhook khÃ´ng há»£p lá»‡! Giao dá»‹ch bá»‹ tá»« chá»‘i.");
        }

        NxvPaymentOrders order = paymentOrdersRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n thanh toÃ¡n vá»›i mÃ£: " + orderCode));

        if (order.getStatus() == 1) {
            throw new AppException(ErrorCode.BAD_REQUEST, "ÄÆ¡n hÃ ng nÃ y Ä‘Ã£ Ä‘Æ°á»£c thanh toÃ¡n vÃ  há»“i sinh nhÃ¢n váº­t trÆ°á»›c Ä‘Ã³ rá»“i!");
        }
        if (payload.getAmountVnd() < order.getAmountVnd()) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Sá»‘ tiá»n chuyá»ƒn khoáº£n (" + payload.getAmountVnd() + "Ä‘) nhá» hÆ¡n giÃ¡ trá»‹ Ä‘Æ¡n hÃ ng (" + order.getAmountVnd() + "Ä‘)!");
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n váº­t!"));

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
        res.put("message", "Thanh toÃ¡n thÃ nh cÃ´ng! NhÃ¢n váº­t [" + c.getCharacterName() + "] Ä‘Ã£ Ä‘Æ°á»£c há»“i sinh 100% HP/MP!");
        return res;
    }

    // API ho tro Demo 1-Click tren Swagger: Tu dong tao chu ky HMAC chuan va goi Webhook hoi sinh luon
    @Transactional
    public Map<String, Object> simulateInstantWebhookSuccessForOrder(String orderCode) {
        NxvPaymentOrders order = paymentOrdersRepository.findByOrderCode(orderCode.trim())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n hÃ ng vá»›i mÃ£: " + orderCode));

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
            case 1 -> "PAID (ÄÃ£ thanh toÃ¡n & Há»“i sinh)";
            case 2 -> "CANCELLED (ÄÃ£ há»§y)";
            default -> "PENDING (Äang chá» quÃ©t mÃ£ QR)";
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
        String subject = "[TaskTale LifeRPG] BiÃªn lai Há»“i sinh NhÃ¢n váº­t - MÃ£ HÄ #" + tx.getBillingTxId();
        String body = "Xin chÃ o " + user.getUsername() + ",\n\n"
                + "Há»‡ thá»‘ng TaskTale / LifeRPG xÃ¡c nháº­n Ä‘Ã£ nháº­n thanh toÃ¡n mua VÃ© Há»“i Sinh thÃ nh cÃ´ng!\n"
                + "--------------------------------------------------\n"
                + "- MÃ£ HÃ³a Ä‘Æ¡n (Billing ID): #" + tx.getBillingTxId() + "\n"
                + "- MÃ£ ÄÆ¡n hÃ ng (Order Code): " + order.getOrderCode() + "\n"
                + "- Sá»‘ tiá»n thanh toÃ¡n: " + String.format("%,d", tx.getAmountVnd()) + " VNÄ\n"
                + "- NhÃ¢n váº­t Ä‘Æ°á»£c há»“i sinh: " + c.getCharacterName() + " (HP: " + c.getCurrentHp() + "/" + c.getMaxHp() + ")\n"
                + (bonusGold > 0 ? "- QuÃ  táº·ng GÃ³i HoÃ ng Kim: +" + bonusGold + " Gold & Báº­t KhiÃªn Cháº¯n\n" : "")
                + "- Thá»i gian xÃ¡c nháº­n: " + tx.getConfirmedAt() + "\n"
                + "--------------------------------------------------\n"
                + "ChÃºc báº¡n giá»¯ vá»¯ng ká»· luáº­t vÃ  khÃ´ng bao giá» Ä‘á»ƒ nhÃ¢n váº­t gá»¥c ngÃ£ ná»¯a nhÃ©!";

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
                .electronicReceiptSummary("HÄ #" + tx.getBillingTxId() + " | ÄÆ¡n " + tx.getOrder().getOrderCode()
                        + " | " + String.format("%,d", tx.getAmountVnd()) + " VNÄ | Há»“i sinh nhÃ¢n váº­t: " + charName)
                .build();
    }
}