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