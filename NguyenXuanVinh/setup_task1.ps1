# ==============================================================================
# TASKTALE / LIFERPG - TASK 1 AUTOMATED GENERATOR (MODULE 1: AUTH & SECURITY)
# Hoan thien Chuc nang #1 -> #12: JWT, OAuth2 Google, OTP, Sessions, Lockout
# ==============================================================================

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Write-JavaFile {
    param([string]$FilePath, [string]$Content)
    $dir = Split-Path -Parent $FilePath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    [System.IO.File]::WriteAllText($FilePath, $Content, $utf8NoBom)
}

$javaRoot = Join-Path (Get-Location) "src\main\java"
$entityDirObj = Get-ChildItem -Path $javaRoot -Recurse -Directory -Filter "nxventity" | Select-Object -First 1
$baseDir = $entityDirObj.Parent.FullName
$basePkg = $baseDir.Substring($javaRoot.Length).TrimStart('\', '/') -replace '[\\/]', '.'

Write-Host "==> Dang sinh Module 1 (Auth, JWT, OTP, OAuth2, Sessions) vao package: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. NANG CAP REPOSITORIES PHUC VU MODULE 1
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\NxvOAuthProvidersRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvOAuthProviders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvOAuthProvidersRepository extends JpaRepository<NxvOAuthProviders, Long> {
    Optional<NxvOAuthProviders> findByProviderAndProviderUserId(String provider, String providerUserId);
    List<NxvOAuthProviders> findByUser_UserId(Long userId);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvOtpTokensRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvOtpTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NxvOtpTokensRepository extends JpaRepository<NxvOtpTokens, Long> {
    Optional<NxvOtpTokens> findTopByUser_UserIdAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(Long userId, Integer tokenType);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvUserSessionsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvUserSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvUserSessionsRepository extends JpaRepository<NxvUserSessions, Long> {
    List<NxvUserSessions> findByUser_UserIdOrderByLastActiveAtDesc(Long userId);
    Optional<NxvUserSessions> findByDeviceTokenAndIsActiveTrue(String deviceToken);
    Optional<NxvUserSessions> findBySessionIdAndUser_UserId(Long sessionId, Long userId);
}
"@

# ==============================================================================
# 2. SINH JWT UTILS & SECURITY UTILS (nxvutils)
# ==============================================================================
Write-JavaFile "$baseDir\nxvutils\NxvJwtUtils.java" @"
package $basePkg.nxvutils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class NxvJwtUtils {

    @Value("`${nxv.jwt.secret:TaskTaleLifeRpgSuperSecretKeyForJwtSigning2026NguyenXuanVinh2310900117}")
    private String jwtSecret;

    @Value("`${nxv.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of(
                        "username", username,
                        "role", role
                ))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }
}
"@

Write-JavaFile "$baseDir\nxvutils\NxvSecurityUtils.java" @"
package $basePkg.nxvutils;

import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvexception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class NxvSecurityUtils {
    private NxvSecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        if (auth != null && auth.getName() != null) {
            try {
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException ignored) {}
        }
        throw new AppException(ErrorCode.UNAUTHORIZED, "Vui lòng đăng nhập (nhập Bearer Token) để thực hiện thao tác này!");
    }
}
"@

# ==============================================================================
# 3. SINH JWT FILTER, SWAGGER BEARER CONFIG & SECURITY CONFIG (nxvconfig)
# ==============================================================================
Write-JavaFile "$baseDir\nxvconfig\NxvJwtAuthenticationFilter.java" @"
package $basePkg.nxvconfig;

import $basePkg.nxvrepository.NxvUserSessionsRepository;
import $basePkg.nxvutils.NxvJwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NxvJwtAuthenticationFilter extends OncePerRequestFilter {

    private final NxvJwtUtils jwtUtils;
    private final NxvUserSessionsRepository sessionsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtils.validateToken(token)) {
                var sessionOpt = sessionsRepository.findByDeviceTokenAndIsActiveTrue(token);
                if (sessionOpt.isPresent()) {
                    var session = sessionOpt.get();
                    session.setLastActiveAt(LocalDateTime.now());
                    sessionsRepository.save(session);

                    Claims claims = jwtUtils.parseClaims(token);
                    Long userId = Long.parseLong(claims.getSubject());
                    Integer role = claims.get("role", Integer.class);
                    String roleName = (role != null && role == 2) ? "ROLE_SUPER_ADMIN"
                            : (role != null && role == 1) ? "ROLE_MODERATOR" : "ROLE_USER";

                    var auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            token,
                            List.of(new SimpleGrantedAuthority(roleName))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
"@

Write-JavaFile "$baseDir\nxvconfig\NxvOpenApiConfig.java" @"
package $basePkg.nxvconfig;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NxvOpenApiConfig {

    @Bean
    public OpenAPI taskTaleOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("TaskTale / LifeRPG API - Đồ Án Tốt Nghiệp")
                        .version("2.2")
                        .description("Hệ thống API Gamification Quản Lý Kỷ Luật & Năng Suất (122 Chức năng - 9 Modules)"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
"@

Write-JavaFile "$baseDir\nxvconfig\NxvSecurityConfig.java" @"
package $basePkg.nxvconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class NxvSecurityConfig {

    private final NxvJwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
"@

# ==============================================================================
# 4. SINH DTO CHO MODULE 1 (nxvdto)
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\NxvAuthDtos.java" @"
package $basePkg.nxvdto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class NxvAuthDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3 đến 50 ký tự")
        private String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
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
        @NotBlank(message = "Google ProviderUserID không được để trống")
        private String providerUserId;

        @NotBlank(message = "Email Google không được để trống")
        @Email
        private String email;

        private String displayName;
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
        private String otpCode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ResetPasswordRequest {
        @NotBlank(message = "Email không được để trống")
        @Email
        private String email;

        @NotBlank(message = "Mã OTP không được để trống")
        private String otpCode;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu mới tối thiểu 6 ký tự")
        private String newPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChangePasswordRequest {
        @NotBlank(message = "Mật khẩu hiện tại không được để trống")
        private String oldPassword;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, message = "Mật khẩu mới tối thiểu 6 ký tự")
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
"@

# ==============================================================================
# 5. SINH SERVICE MODULE 1 (nxvservice)
# ==============================================================================
Write-JavaFile "$baseDir\nxvservice\NxvLoginAttemptService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxventity.Users;
import $basePkg.nxvrepository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NxvLoginAttemptService {

    private final UsersRepository usersRepository;
    public static final int MAX_FAIL_ATTEMPTS = 5;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedLogin(Long userId) {
        Users user = usersRepository.findById(userId).orElseThrow();
        int fails = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        user.setLoginFailCount(fails);
        if (fails >= MAX_FAIL_ATTEMPTS) {
            user.setIsLocked(true);
        }
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
        return fails;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedLogin(Long userId) {
        Users user = usersRepository.findById(userId).orElseThrow();
        user.setLoginFailCount(0);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
    }
}
"@

Write-JavaFile "$baseDir\nxvservice\NxvAuthService.java" @"
package $basePkg.nxvservice;

import $basePkg.nxvdto.NxvAuthDtos.*;
import $basePkg.nxventity.*;
import $basePkg.nxvenums.ErrorCode;
import $basePkg.nxvenums.NxvConstants;
import $basePkg.nxvexception.AppException;
import $basePkg.nxvrepository.*;
import $basePkg.nxvutils.NxvJwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvAuthService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvUserInventoryConfigRepository inventoryConfigRepository;
    private final NxvTitlesRepository titlesRepository;
    private final NxvUserTitlesRepository userTitlesRepository;
    private final NxvOAuthProvidersRepository oauthProvidersRepository;
    private final NxvOtpTokensRepository otpTokensRepository;
    private final NxvUserSessionsRepository userSessionsRepository;
    private final NxvLoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;
    private final NxvJwtUtils jwtUtils;

    private static final String DEFAULT_MALE_AVATAR = "/images/avatars/static_male_hero.png";
    private static final String DEFAULT_FEMALE_AVATAR = "/images/avatars/static_female_heroine.png";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Chuc nang #1, #2 & #12: Dang ky + Map Gioi tinh voi Avatar 2D tinh + Khoi tao Character & Inventory
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (usersRepository.existsByUsername(req.getUsername().trim())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Username đã tồn tại trong hệ thống!");
        }
        if (usersRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Email đã được sử dụng!");
        }

        int gender = (req.getGender() != null && req.getGender() == NxvConstants.GENDER_FEMALE)
                ? NxvConstants.GENDER_FEMALE : NxvConstants.GENDER_MALE;
        String defaultAvatar = (gender == NxvConstants.GENDER_FEMALE) ? DEFAULT_FEMALE_AVATAR : DEFAULT_MALE_AVATAR;

        Users newUser = Users.builder()
                .username(req.getUsername().trim())
                .email(req.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .gender(gender)
                .defaultAvatarUrl(defaultAvatar)
                .role(NxvConstants.ROLE_USER)
                .isEmailVerified(false)
                .isLocked(false)
                .isDead(false)
                .loginFailCount(0)
                .lastLoginAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        newUser = usersRepository.save(newUser);

        // Tang danh hieu khoi dau 'Tan Binh' (TitleID = 1) neu co
        NxvTitles starterTitle = titlesRepository.findById(1).orElse(null);
        if (starterTitle != null) {
            userTitlesRepository.save(NxvUserTitles.builder()
                    .user(newUser)
                    .title(starterTitle)
                    .unlockedAt(LocalDateTime.now())
                    .build());
        }

        // Khoi tao NxvCharacters (Chuc nang #12 - Schema v2.2 BaseMaxHP/MaxHP)
        String charName = (req.getCharacterName() != null && !req.getCharacterName().isBlank())
                ? req.getCharacterName().trim() : newUser.getUsername();

        NxvCharacters character = NxvCharacters.builder()
                .user(newUser)
                .characterName(charName)
                .avatarUrl(defaultAvatar)
                .currentLevel(1)
                .currentExp(0L)
                .expToNextLevel(100L)
                .baseMaxHp(100)
                .baseMaxMp(50)
                .maxHp(100)
                .currentHp(100)
                .maxMp(50)
                .currentMp(50)
                .gold(100L) // Tang 100 Gold khoi nghiep
                .skillPoints(0)
                .isShieldActive(false)
                .totalTasksDone(0)
                .currentTitle(starterTitle)
                .streakDays(0)
                .updatedAt(LocalDateTime.now())
                .build();
        character = charactersRepository.save(character);

        // Khoi tao NxvUserInventoryConfig (50 o tui do mac dinh)
        inventoryConfigRepository.save(NxvUserInventoryConfig.builder()
                .userId(newUser.getUserId())
                .maxSlots(50)
                .build());

        return createSessionAndBuildResponse(newUser, character, req.getDeviceName());
    }

    // Chuc nang #3 & #11: Dang nhap JWT + Tu dong khoa tai khoan khi sai mat khau 5 lan
    @Transactional
    public AuthResponse login(LoginRequest req) {
        String key = req.getUsernameOrEmail().trim();
        Users user = usersRepository.findByUsername(key)
                .or(() -> usersRepository.findByEmail(key.toLowerCase()))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác!"));

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tài khoản đã bị khóa do nhập sai mật khẩu quá 5 lần! Vui lòng dùng tính năng Quên mật khẩu để mở khóa.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            int fails = loginAttemptService.recordFailedLogin(user.getUserId());
            int remaining = Math.max(0, NxvLoginAttemptService.MAX_FAIL_ATTEMPTS - fails);
            if (remaining == 0) {
                throw new AppException(ErrorCode.FORBIDDEN, "Bạn đã nhập sai 5 lần liên tiếp. Tài khoản đã bị khóa tự động!");
            }
            throw new AppException(ErrorCode.UNAUTHORIZED, "Mật khẩu không chính xác! Còn " + remaining + " lần thử trước khi bị khóa.");
        }

        loginAttemptService.resetFailedLogin(user.getUserId());
        NxvCharacters character = getOrCreateCharacter(user);
        return createSessionAndBuildResponse(user, character, req.getDeviceName());
    }

    // Chuc nang #4: Dang nhap qua Google (OAuth2)
    @Transactional
    public AuthResponse googleOAuthLogin(GoogleOAuthRequest req) {
        var oauthOpt = oauthProvidersRepository.findByProviderAndProviderUserId("GOOGLE", req.getProviderUserId());
        Users user;

        if (oauthOpt.isPresent()) {
            user = oauthOpt.get().getUser();
        } else {
            String email = req.getEmail().trim().toLowerCase();
            user = usersRepository.findByEmail(email).orElseGet(() -> {
                int gender = (req.getGender() != null && req.getGender() == 1) ? 1 : 0;
                String avatar = (gender == 1) ? DEFAULT_FEMALE_AVATAR : DEFAULT_MALE_AVATAR;
                String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
                String finalUsername = usersRepository.existsByUsername(baseUsername)
                        ? baseUsername + "_" + (1000 + RANDOM.nextInt(9000)) : baseUsername;

                Users created = Users.builder()
                        .username(finalUsername)
                        .email(email)
                        .gender(gender)
                        .defaultAvatarUrl(avatar)
                        .role(NxvConstants.ROLE_USER)
                        .isEmailVerified(true)
                        .isLocked(false)
                        .isDead(false)
                        .loginFailCount(0)
                        .lastLoginAt(LocalDateTime.now())
                        .build();
                return usersRepository.save(created);
            });

            oauthProvidersRepository.save(NxvOAuthProviders.builder()
                    .user(user)
                    .provider("GOOGLE")
                    .providerUserId(req.getProviderUserId())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tài khoản này đang bị khóa!");
        }

        NxvCharacters character = getOrCreateCharacter(user);
        return createSessionAndBuildResponse(user, character,
                req.getDeviceName() != null ? req.getDeviceName() : "Google OAuth2 Web");
    }

    // Chuc nang #5: Gui OTP & Xac thuc Email (TokenType = 0)
    @Transactional
    public Map<String, Object> sendEmailVerificationOtp(SendOtpRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản với Email này!"));
        return generateAndSaveOtp(user, 0, "Mã OTP xác thực tài khoản đã được khởi tạo!");
    }

    @Transactional
    public Map<String, Object> verifyEmailOtp(VerifyEmailOtpRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản với Email này!"));

        validateOtpToken(user.getUserId(), 0, req.getOtpCode());
        user.setIsEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        return Map.of("email", user.getEmail(), "isEmailVerified", true);
    }

    // Chuc nang #6: Quen mat khau & Dat lai mat khau qua OTP (TokenType = 1)
    @Transactional
    public Map<String, Object> forgotPasswordSendOtp(SendOtpRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản với Email này!"));
        return generateAndSaveOtp(user, 1, "Mã OTP khôi phục mật khẩu đã được khởi tạo!");
    }

    @Transactional
    public Map<String, Object> resetPasswordWithOtp(ResetPasswordRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản với Email này!"));

        validateOtpToken(user.getUserId(), 1, req.getOtpCode());
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setIsLocked(false);
        user.setLoginFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        return Map.of("email", user.getEmail(), "status", "Đã đặt lại mật khẩu và mở khóa tài khoản thành công!");
    }

    // Chuc nang #7: Thay doi mat khau
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (user.getPasswordHash() != null && !passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mật khẩu hiện tại không chính xác!");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
    }

    // Chuc nang #8: Xem & Cap nhat Profile ca nhan
    @Transactional(readOnly = true)
    public AuthResponse getMyProfile(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        NxvCharacters character = getOrCreateCharacter(user);
        return buildAuthResponse(user, character, null, null);
    }

    @Transactional
    public AuthResponse updateMyProfile(Long userId, UpdateProfileRequest req) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        NxvCharacters character = getOrCreateCharacter(user);

        if (req.getGender() != null) {
            int gender = (req.getGender() == 1) ? 1 : 0;
            user.setGender(gender);
            if (req.getDefaultAvatarUrl() == null || req.getDefaultAvatarUrl().isBlank()) {
                String defaultAvatar = (gender == 1) ? DEFAULT_FEMALE_AVATAR : DEFAULT_MALE_AVATAR;
                user.setDefaultAvatarUrl(defaultAvatar);
                character.setAvatarUrl(defaultAvatar);
            }
        }
        if (req.getDefaultAvatarUrl() != null && !req.getDefaultAvatarUrl().isBlank()) {
            user.setDefaultAvatarUrl(req.getDefaultAvatarUrl().trim());
            character.setAvatarUrl(req.getDefaultAvatarUrl().trim());
        }
        if (req.getCharacterName() != null && !req.getCharacterName().isBlank()) {
            character.setCharacterName(req.getCharacterName().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        character.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
        charactersRepository.save(character);

        return buildAuthResponse(user, character, null, null);
    }

    // Chuc nang #9: Quan ly danh sach thiet bi dang dang nhap (Sessions)
    @Transactional(readOnly = true)
    public List<SessionDto> getMySessions(Long userId) {
        return userSessionsRepository.findByUser_UserIdOrderByLastActiveAtDesc(userId).stream()
                .map(s -> SessionDto.builder()
                        .sessionId(s.getSessionId())
                        .deviceName(s.getDeviceName())
                        .isActive(s.getIsActive())
                        .lastActiveAt(s.getLastActiveAt())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeSession(Long userId, Long sessionId) {
        NxvUserSessions session = userSessionsRepository.findBySessionIdAndUser_UserId(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy phiên đăng nhập này!"));
        session.setIsActive(false);
        userSessionsRepository.save(session);
    }

    // Chuc nang #10: Dang xuat an toan
    @Transactional
    public void logoutCurrentToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            userSessionsRepository.findByDeviceTokenAndIsActiveTrue(token).ifPresent(s -> {
                s.setIsActive(false);
                userSessionsRepository.save(s);
            });
        }
    }

    // --- Helpers ---
    private Map<String, Object> generateAndSaveOtp(Users user, int tokenType, String message) {
        String rawOtp = String.format("%06d", RANDOM.nextInt(1000000));
        NxvOtpTokens otp = NxvOtpTokens.builder()
                .user(user)
                .tokenType(tokenType)
                .tokenHash(passwordEncoder.encode(rawOtp))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpTokensRepository.save(otp);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("email", user.getEmail());
        res.put("expiresInMinutes", 10);
        res.put("otpDebugCode", rawOtp); // Giup test ngay tren Swagger khong can SMTP
        res.put("note", message);
        return res;
    }

    private void validateOtpToken(Long userId, int tokenType, String rawOtp) {
        NxvOtpTokens token = otpTokensRepository
                .findTopByUser_UserIdAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(userId, tokenType)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã OTP hợp lệ hoặc mã đã được sử dụng!"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP đã hết hạn (quá 10 phút). Vui lòng yêu cầu mã mới!");
        }
        if (!passwordEncoder.matches(rawOtp.trim(), token.getTokenHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP không chính xác!");
        }
        token.setIsUsed(true);
        otpTokensRepository.save(token);
    }

    private NxvCharacters getOrCreateCharacter(Users user) {
        return charactersRepository.findByUser_UserId(user.getUserId()).orElseGet(() -> {
            NxvCharacters c = NxvCharacters.builder()
                    .user(user)
                    .characterName(user.getUsername())
                    .avatarUrl(user.getDefaultAvatarUrl() != null ? user.getDefaultAvatarUrl() : DEFAULT_MALE_AVATAR)
                    .currentLevel(1)
                    .currentExp(0L)
                    .expToNextLevel(100L)
                    .baseMaxHp(100)
                    .baseMaxMp(50)
                    .maxHp(100)
                    .currentHp(100)
                    .maxMp(50)
                    .currentMp(50)
                    .gold(100L)
                    .build();
            c = charactersRepository.save(c);
            if (!inventoryConfigRepository.existsById(user.getUserId())) {
                inventoryConfigRepository.save(NxvUserInventoryConfig.builder()
                        .userId(user.getUserId())
                        .maxSlots(50)
                        .build());
            }
            return c;
        });
    }

    private AuthResponse createSessionAndBuildResponse(Users user, NxvCharacters character, String deviceName) {
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        NxvUserSessions session = NxvUserSessions.builder()
                .user(user)
                .deviceName(deviceName != null && !deviceName.isBlank() ? deviceName : "Web Browser (ReactJS/Swagger)")
                .deviceToken(token)
                .isActive(true)
                .lastActiveAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        session = userSessionsRepository.save(session);
        return buildAuthResponse(user, character, token, session.getSessionId());
    }

    private AuthResponse buildAuthResponse(Users user, NxvCharacters character, String token, Long sessionId) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(token != null ? "Bearer" : null)
                .sessionId(sessionId)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .gender(user.getGender())
                .defaultAvatarUrl(user.getDefaultAvatarUrl())
                .role(user.getRole())
                .isEmailVerified(user.getIsEmailVerified())
                .isDead(user.getIsDead())
                .characterId(character.getCharacterId())
                .characterName(character.getCharacterName())
                .currentLevel(character.getCurrentLevel())
                .currentHp(character.getCurrentHp())
                .maxHp(character.getMaxHp())
                .currentMp(character.getCurrentMp())
                .maxMp(character.getMaxMp())
                .gold(character.getGold())
                .build();
    }
}
"@

# ==============================================================================
# 6. SINH REST CONTROLLER CHO MODULE 1 (nxvcontroller)
# ==============================================================================
Write-JavaFile "$baseDir\nxvcontroller\NxvAuthController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvdto.NxvAuthDtos.*;
import $basePkg.nxvservice.NxvAuthService;
import $basePkg.nxvutils.NxvSecurityUtils;
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
"@

Write-Host "==> HOAN TAT SINH MODULE 1 (TASK 1: 11 CHUC NANG AUTH + KHOI TAO NHAN VAT) THANH CONG!" -ForegroundColor Green