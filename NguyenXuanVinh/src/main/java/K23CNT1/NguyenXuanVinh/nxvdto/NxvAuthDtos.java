package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class NxvAuthDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Username khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Size(min = 3, max = 50, message = "Username tá»« 3 Ä‘áº¿n 50 kÃ½ tá»±")
        private String username;

        @NotBlank(message = "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Email(message = "Email khÃ´ng Ä‘Ãºng Ä‘á»‹nh dáº¡ng")
        private String email;

        @NotBlank(message = "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Size(min = 6, message = "Máº­t kháº©u tá»‘i thiá»ƒu 6 kÃ½ tá»±")
        private String password;

        @NotNull(message = "Giá»›i tÃ­nh (0=Nam, 1=Ná»¯) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private Integer gender;

        private String characterName;
        private String deviceName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest {
        @NotBlank(message = "Username hoáº·c Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String usernameOrEmail;

        @NotBlank(message = "Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String password;

        private String deviceName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GoogleOAuthRequest {
        @NotBlank(message = "Google ProviderUserID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String providerUserId;

        @NotBlank(message = "Email Google khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Email
        private String email;

        private String displayName;
        private Integer gender;
        private String deviceName;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendOtpRequest {
        @NotBlank(message = "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Email
        private String email;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VerifyEmailOtpRequest {
        @NotBlank(message = "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Email
        private String email;

        @NotBlank(message = "MÃ£ OTP khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String otpCode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResetPasswordRequest {
        @NotBlank(message = "Email khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Email
        private String email;

        @NotBlank(message = "MÃ£ OTP khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String otpCode;

        @NotBlank(message = "Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Size(min = 6, message = "Máº­t kháº©u má»›i tá»‘i thiá»ƒu 6 kÃ½ tá»±")
        private String newPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "Máº­t kháº©u hiá»‡n táº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String oldPassword;

        @NotBlank(message = "Máº­t kháº©u má»›i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Size(min = 6, message = "Máº­t kháº©u má»›i tá»‘i thiá»ƒu 6 kÃ½ tá»±")
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