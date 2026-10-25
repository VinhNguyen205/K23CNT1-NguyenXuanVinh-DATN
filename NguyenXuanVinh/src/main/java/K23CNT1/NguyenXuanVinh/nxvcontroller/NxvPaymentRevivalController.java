package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvPaymentRevivalDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvPaymentRevivalService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
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
@Tag(name = "Module 9: Cá»•ng Thanh toÃ¡n VietQR/Momo & Tráº¡ng thÃ¡i Sinh tá»­ (Chá»©c nÄƒng #116 - #122)", description = "Tráº¡ng thÃ¡i Tá»­ vong (Death State), Sinh QR Äá»™ng VietQR/Momo, Webhook HMAC-SHA256, Há»“i sinh NhÃ¢n váº­t, HÃ³a Ä‘Æ¡n Billing & Gá»­i Email BiÃªn lai")
public class NxvPaymentRevivalController {

    private final NxvPaymentRevivalService paymentRevivalService;

    @Operation(summary = "[#116] Kiá»ƒm tra Tráº¡ng thÃ¡i Sinh tá»­ (Death State) vÃ  danh sÃ¡ch GÃ³i VÃ© Há»“i Sinh")
    @GetMapping("/death-state/me")
    public ApiResponse<DeathStateStatusDto> getMyDeathState() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(paymentRevivalService.getDeathStateStatus(userId));
    }

    @Operation(summary = "[#116-DEMO] Giáº£ láº­p NhÃ¢n váº­t háº¿t mÃ¡u (HP = 0 -> IsDead = true) Ä‘á»ƒ test khÃ³a tÆ°Æ¡ng tÃ¡c & Há»“i sinh khi báº£o vá»‡ Ä‘á»“ Ã¡n")
    @PostMapping("/death-state/simulate-death")
    public ApiResponse<DeathStateStatusDto> simulateDeath() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ kÃ­ch hoáº¡t tráº¡ng thÃ¡i Tá»­ vong (HP = 0, IsDead = true)!", paymentRevivalService.simulateCharacterDeathForDemo(userId));
    }

    @Operation(summary = "[#117] Xem danh sÃ¡ch cÃ¡c GÃ³i VÃ© Há»“i Sinh (VÃ© CÆ¡ Báº£n 20.000Ä‘ / VÃ© HoÃ ng Kim 50.000Ä‘)")
    @GetMapping("/packages")
    public ApiResponse<List<RevivalPackageDto>> getPackages() {
        return ApiResponse.ok(paymentRevivalService.getAllRevivalPackages());
    }

    @Operation(summary = "[#117, #118] Táº¡o Ä‘Æ¡n mua VÃ© Há»“i Sinh & Sinh mÃ£ QR Äá»™ng (VietQR / Momo) chá»©a sáºµn sá»‘ tiá»n vÃ  ná»™i dung chuyá»ƒn khoáº£n theo UserID")
    @PostMapping("/orders")
    public ApiResponse<PaymentOrderQrResponse> createRevivalOrder(@Valid @RequestBody CreateRevivalOrderRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ sinh Ä‘Æ¡n hÃ ng vÃ  mÃ£ QR Äá»™ng VietQR/Momo thÃ nh cÃ´ng!", paymentRevivalService.createRevivalPaymentOrder(userId, request));
    }

    @Operation(summary = "[#118B] Xem lá»‹ch sá»­ cÃ¡c Ä‘Æ¡n Ä‘áº·t mua VÃ© Há»“i Sinh cá»§a tÃ´i")
    @GetMapping("/orders/me")
    public ApiResponse<List<PaymentOrderQrResponse>> getMyOrders() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(paymentRevivalService.getMyPaymentOrders(userId));
    }

    @Operation(summary = "[#119, #120, #122] Webhook/IPN Listener: Nháº­n tÃ­n hiá»‡u tá»« NgÃ¢n hÃ ng, kiá»ƒm tra chá»¯ kÃ½ HMAC-SHA256, má»Ÿ khÃ³a Há»“i sinh ngay láº­p tá»©c & gá»­i Email biÃªn lai")
    @PostMapping("/webhook/bank-ipn")
    public ApiResponse<Map<String, Object>> handleBankWebhook(@Valid @RequestBody BankWebhookPayload payload) {
        return ApiResponse.ok("XÃ¡c thá»±c Webhook thÃ nh cÃ´ng! NhÃ¢n váº­t Ä‘Ã£ Ä‘Æ°á»£c há»“i sinh.", paymentRevivalService.processBankPaymentWebhook(payload));
    }

    @Operation(summary = "[#119-DEMO] NÃºt báº¥m Giáº£ láº­p NgÃ¢n hÃ ng bÃ¡o tiá»n vá» thÃ nh cÃ´ng (Tá»± kÃ½ HMAC-SHA256 & kÃ­ch hoáº¡t Webhook há»“i sinh ngay cho orderCode)")
    @PostMapping("/webhook/simulate-paid/{orderCode}")
    public ApiResponse<Map<String, Object>> simulatePaidWebhook(@PathVariable String orderCode) {
        return ApiResponse.ok("Giáº£ láº­p tÃ­n hiá»‡u chuyá»ƒn khoáº£n thÃ nh cÃ´ng! NhÃ¢n váº­t Ä‘Ã£ há»“i sinh.", paymentRevivalService.simulateInstantWebhookSuccessForOrder(orderCode));
    }

    @Operation(summary = "[#121, #122] Xem danh sÃ¡ch HÃ³a Ä‘Æ¡n Ä‘iá»‡n tá»­ (Billing Transactions) cá»§a tÃ´i")
    @GetMapping("/billing/me")
    public ApiResponse<List<BillingInvoiceDto>> getMyBillingInvoices() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(paymentRevivalService.getMyBillingInvoices(userId));
    }
}