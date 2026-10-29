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
@Tag(name = "Module 1: Tài khoản & Phân quyền (Chức năng #1 - #11)", description = "Đăng ký chọn Giới tính map Avatar tĩnh, JWT Login, Google OAuth2, OTP, Sessions & Khóa tự động")
public class NxvAuthController {

    private final NxvAuthService authService;

    @Operation(summary = "[#1, #2, #12] Đăng ký tài khoản + Chọn Giới tính (0=Nam, 1=Nữ) map Avatar tĩnh + Khởi tạo Nhân vật")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Đăng ký tài khoản và khởi tạo nhân vật thành công!", authService.register(request));
    }

    @Operation(summary = "[#3, #11] Đăng nhập Email/Username + Password (Trả về JWT, sai 5 lần tự động khóa)")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Đăng nhập thành công!", authService.login(request));
    }

    @Operation(summary = "[#4] Đăng nhập qua Google (OAuth2)")
    @PostMapping("/oauth2/google")
    public ApiResponse<AuthResponse> googleLogin(@Valid @RequestBody GoogleOAuthRequest request) {
        return ApiResponse.ok("Đăng nhập Google OAuth2 thành công!", authService.googleOAuthLogin(request));
    }

    @Operation(summary = "[#5A] Gửi mã OTP xác thực Email")
    @PostMapping("/otp/send-verify-email")
    public ApiResponse<Map<String, Object>> sendVerifyEmailOtp(@Valid @RequestBody SendOtpRequest request) {
        return ApiResponse.ok("Đã gửi mã OTP xác thực email!", authService.sendEmailVerificationOtp(request));
    }

    @Operation(summary = "[#5B] Xác thực tài khoản bằng mã OTP")
    @PostMapping("/otp/verify-email")
    public ApiResponse<Map<String, Object>> verifyEmailOtp(@Valid @RequestBody VerifyEmailOtpRequest request) {
        return ApiResponse.ok("Xác thực email thành công!", authService.verifyEmailOtp(request));
    }

    @Operation(summary = "[#6A] Quên mật khẩu - Yêu cầu gửi mã OTP")
    @PostMapping("/password/forgot")
    public ApiResponse<Map<String, Object>> forgotPassword(@Valid @RequestBody SendOtpRequest request) {
        return ApiResponse.ok("Đã gửi mã OTP khôi phục mật khẩu!", authService.forgotPasswordSendOtp(request));
    }

    @Operation(summary = "[#6B] Đặt lại mật khẩu mới bằng OTP (Đồng thời mở khóa tài khoản nếu bị khóa)")
    @PostMapping("/password/reset")
    public ApiResponse<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.ok("Đặt lại mật khẩu thành công!", authService.resetPasswordWithOtp(request));
    }

    @Operation(summary = "[#7] Thay đổi mật khẩu (Yêu cầu đăng nhập)")
    @PutMapping("/password/change")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        authService.changePassword(userId, request);
        return ApiResponse.ok("Thay đổi mật khẩu thành công!", null);
    }

    @Operation(summary = "[#8A] Xem thông tin Profile & Nhân vật hiện tại")
    @GetMapping("/me")
    public ApiResponse<AuthResponse> getMyProfile() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(authService.getMyProfile(userId));
    }

    @Operation(summary = "[#8B] Cập nhật thông tin Profile cá nhân (Tên nhân vật, Giới tính, Avatar tĩnh)")
    @PutMapping("/profile")
    public ApiResponse<AuthResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật thông tin cá nhân thành công!", authService.updateMyProfile(userId, request));
    }

    @Operation(summary = "[#9A] Quản lý danh sách thiết bị đang đăng nhập (Sessions)")
    @GetMapping("/sessions")
    public ApiResponse<List<SessionDto>> getMySessions() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(authService.getMySessions(userId));
    }

    @Operation(summary = "[#9B] Thu hồi / Đăng xuất từ xa một thiết bị theo SessionID")
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> revokeSession(@PathVariable Long sessionId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        authService.revokeSession(userId, sessionId);
        return ApiResponse.ok("Đã thu hồi phiên đăng nhập của thiết bị!", null);
    }

    @Operation(summary = "[#10] Đăng xuất an toàn (Vô hiệu hóa Token hiện tại)")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logoutCurrentToken(request.getHeader("Authorization"));
        return ApiResponse.ok("Đăng xuất an toàn thành công!", null);
    }
}