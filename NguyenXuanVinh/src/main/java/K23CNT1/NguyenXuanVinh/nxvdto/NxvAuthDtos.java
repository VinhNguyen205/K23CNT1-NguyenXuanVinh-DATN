package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class NxvAuthDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3 đến 50 ký tự")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username chỉ gồm chữ cái, số và dấu gạch dưới")
        private String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
        private String password;

        @NotNull(message = "Giới tính (0=Nam, 1=Nữ) không được để trống")
        private Integer gender;

        private String characterName;
        private String deviceName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {
        @NotBlank(message = "Username hoặc Email không được để trống")
        private String usernameOrEmail;

        @NotBlank(message = "Mật khẩu không được để trống")
        private String password;

        private String deviceName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GoogleOAuthRequest {
        /** ID Token (JWT) mà Google Identity Services trả về cho frontend. Backend tự xác thực với Google. */
        @NotBlank(message = "Google ID Token không được để trống")
        private String idToken;

        private Integer gender;
        private String deviceName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendOtpRequest {
        @NotBlank(message = "Email không được để trống")
        @Email
        private String email;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VerifyEmailOtpRequest {
        @NotBlank(message = "Email không được để trống")
        @Email
        private String email;

        @NotBlank(message = "Mã OTP không được để trống")
        @Pattern(regexp = "^\\d{6}$", message = "Mã OTP gồm đúng 6 chữ số")
        private String otpCode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResetPasswordRequest {
        @NotBlank(message = "Email không được để trống")
        @Email
        private String email;

        @NotBlank(message = "Mã OTP không được để trống")
        @Pattern(regexp = "^\\d{6}$", message = "Mã OTP gồm đúng 6 chữ số")
        private String otpCode;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu mới từ 8 đến 72 ký tự")
        private String newPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "Mật khẩu hiện tại không được để trống")
        private String oldPassword;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu mới từ 8 đến 72 ký tự")
        private String newPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateProfileRequest {
        private String characterName;
        private Integer gender;
        private String defaultAvatarUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String accessToken;
        @Builder.Default
        private String tokenType = "Bearer";
        private Long sessionId;
        private Long userId;
        private String username;
        private String email;
        private Integer gender;
        private String defaultAvatarUrl;
        private Integer role;
        private Boolean isEmailVerified;
        private Boolean isDead;
        private Long characterId;
        private String characterName;
        private Integer currentLevel;
        private Integer currentHp;
        private Integer maxHp;
        private Integer currentMp;
        private Integer maxMp;
        private Long gold;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SessionDto {
        private Long sessionId;
        private String deviceName;
        private Boolean isActive;
        private LocalDateTime lastActiveAt;
        private LocalDateTime createdAt;
    }
}