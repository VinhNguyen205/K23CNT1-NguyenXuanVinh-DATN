package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvAuthDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvAuthService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Module 1: TÃ i khoáº£n & PhÃ¢n quyá»n (Chá»©c nÄƒng #1 - #11)", description = "ÄÄƒng kÃ½ chá»n Giá»›i tÃ­nh map Avatar tÄ©nh, JWT Login, Google OAuth2, OTP, Sessions & KhÃ³a tá»± Ä‘á»™ng")
public class NxvAuthController {

    private final NxvAuthService authService;

    @Operation(summary = "[#1, #2, #12] ÄÄƒng kÃ½ tÃ i khoáº£n + Chá»n Giá»›i tÃ­nh (0=Nam, 1=Ná»¯) map Avatar tÄ©nh + Khá»Ÿi táº¡o NhÃ¢n váº­t")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("ÄÄƒng kÃ½ tÃ i khoáº£n vÃ  khá»Ÿi táº¡o nhÃ¢n váº­t thÃ nh cÃ´ng!", authService.register(request));
    }

    @Operation(summary = "[#3, #11] ÄÄƒng nháº­p Email/Username + Password (Tráº£ vá» JWT, sai 5 láº§n tá»± Ä‘á»™ng khÃ³a)")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("ÄÄƒng nháº­p thÃ nh cÃ´ng!", authService.login(request));
    }

    @Operation(summary = "[#4] ÄÄƒng nháº­p qua Google (OAuth2)")
    @PostMapping("/oauth2/google")
    public ApiResponse<AuthResponse> googleLogin(@Valid @RequestBody GoogleOAuthRequest request) {
        return ApiResponse.ok("ÄÄƒng nháº­p Google OAuth2 thÃ nh cÃ´ng!", authService.googleOAuthLogin(request));
    }

    @Operation(summary = "[#5A] Gá»­i mÃ£ OTP xÃ¡c thá»±c Email")
    @PostMapping("/otp/send-verify-email")
    public ApiResponse<Map<String, Object>> sendVerifyEmailOtp(@Valid @RequestBody SendOtpRequest request) {
        return ApiResponse.ok("ÄÃ£ gá»­i mÃ£ OTP xÃ¡c thá»±c email!", authService.sendEmailVerificationOtp(request));
    }

    @Operation(summary = "[#5B] XÃ¡c thá»±c tÃ i khoáº£n báº±ng mÃ£ OTP")
    @PostMapping("/otp/verify-email")
    public ApiResponse<Map<String, Object>> verifyEmailOtp(@Valid @RequestBody VerifyEmailOtpRequest request) {
        return ApiResponse.ok("XÃ¡c thá»±c email thÃ nh cÃ´ng!", authService.verifyEmailOtp(request));
    }

    @Operation(summary = "[#6A] QuÃªn máº­t kháº©u - YÃªu cáº§u gá»­i mÃ£ OTP")
    @PostMapping("/password/forgot")
    public ApiResponse<Map<String, Object>> forgotPassword(@Valid @RequestBody SendOtpRequest request) {
        return ApiResponse.ok("ÄÃ£ gá»­i mÃ£ OTP khÃ´i phá»¥c máº­t kháº©u!", authService.forgotPasswordSendOtp(request));
    }

    @Operation(summary = "[#6B] Äáº·t láº¡i máº­t kháº©u má»›i báº±ng OTP (Äá»“ng thá»i má»Ÿ khÃ³a tÃ i khoáº£n náº¿u bá»‹ khÃ³a)")
    @PostMapping("/password/reset")
    public ApiResponse<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.ok("Äáº·t láº¡i máº­t kháº©u thÃ nh cÃ´ng!", authService.resetPasswordWithOtp(request));
    }

    @Operation(summary = "[#7] Thay Ä‘á»•i máº­t kháº©u (YÃªu cáº§u Ä‘Äƒng nháº­p)")
    @PutMapping("/password/change")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        authService.changePassword(userId, request);
        return ApiResponse.ok("Thay Ä‘á»•i máº­t kháº©u thÃ nh cÃ´ng!", null);
    }

    @Operation(summary = "[#8A] Xem thÃ´ng tin Profile & NhÃ¢n váº­t hiá»‡n táº¡i")
    @GetMapping("/me")
    public ApiResponse<AuthResponse> getMyProfile() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(authService.getMyProfile(userId));
    }

    @Operation(summary = "[#8B] Cáº­p nháº­t thÃ´ng tin Profile cÃ¡ nhÃ¢n (TÃªn nhÃ¢n váº­t, Giá»›i tÃ­nh, Avatar tÄ©nh)")
    @PutMapping("/profile")
    public ApiResponse<AuthResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t thÃ´ng tin cÃ¡ nhÃ¢n thÃ nh cÃ´ng!", authService.updateMyProfile(userId, request));
    }

    @Operation(summary = "[#9A] Quáº£n lÃ½ danh sÃ¡ch thiáº¿t bá»‹ Ä‘ang Ä‘Äƒng nháº­p (Sessions)")
    @GetMapping("/sessions")
    public ApiResponse<List<SessionDto>> getMySessions() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(authService.getMySessions(userId));
    }

    @Operation(summary = "[#9B] Thu há»“i / ÄÄƒng xuáº¥t tá»« xa má»™t thiáº¿t bá»‹ theo SessionID")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> revokeSession(@PathVariable Long sessionId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        authService.revokeSession(userId, sessionId);
        return ApiResponse.ok("ÄÃ£ thu há»“i phiÃªn Ä‘Äƒng nháº­p cá»§a thiáº¿t bá»‹!", null);
    }

    @Operation(summary = "[#10] ÄÄƒng xuáº¥t an toÃ n (VÃ´ hiá»‡u hÃ³a Token hiá»‡n táº¡i)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logoutCurrentToken(request.getHeader("Authorization"));
        return ApiResponse.ok("ÄÄƒng xuáº¥t an toÃ n thÃ nh cÃ´ng!", null);
    }
}