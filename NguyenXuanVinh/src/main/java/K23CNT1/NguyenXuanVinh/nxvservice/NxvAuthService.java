package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvAuthDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvJwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private final NxvGoogleTokenVerifier googleTokenVerifier;

    /** true = trả mã OTP trong response (chỉ để dev/test trên Swagger khi chưa cấu hình SMTP). PHẢI tắt khi deploy. */
    @Value("${nxv.otp.debug-enabled:false}")
    private boolean otpDebugEnabled;

    private static final String DEFAULT_MALE_AVATAR = "/images/avatars/static_male_hero.png";
    private static final String DEFAULT_FEMALE_AVATAR = "/images/avatars/static_female_heroine.png";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String STARTER_TITLE_NAME = "Tân Binh";
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;

    /** Đếm số lần nhập sai OTP theo TokenID (trong bộ nhớ, đủ cho 1 instance; muốn scale nhiều server thì chuyển sang cột DB/Redis). */
    private final Map<Long, Integer> otpFailedAttempts = new ConcurrentHashMap<>();

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

        NxvCharacters character = initNewPlayer(newUser, req.getCharacterName());

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

    // Chuc nang #4: Dang nhap qua Google (OAuth2) - ID Token duoc xac thuc voi Google, khong tin du lieu client tu khai
    @Transactional
    public AuthResponse googleOAuthLogin(GoogleOAuthRequest req) {
        var identity = googleTokenVerifier.verify(req.getIdToken());
        boolean isNewUser = false;
        Users user;

        var oauthOpt = oauthProvidersRepository.findByProviderAndProviderUserId("GOOGLE", identity.subject());
        if (oauthOpt.isPresent()) {
            user = oauthOpt.get().getUser();
        } else {
            var existing = usersRepository.findByEmail(identity.email());
            if (existing.isPresent()) {
                user = existing.get(); // Email Google da xac minh -> lien ket vao tai khoan cung email
            } else {
                int gender = (req.getGender() != null && req.getGender() == NxvConstants.GENDER_FEMALE)
                        ? NxvConstants.GENDER_FEMALE : NxvConstants.GENDER_MALE;
                String avatar = (gender == NxvConstants.GENDER_FEMALE) ? DEFAULT_FEMALE_AVATAR : DEFAULT_MALE_AVATAR;
                user = usersRepository.save(Users.builder()
                        .username(generateUniqueUsername(identity.email()))
                        .email(identity.email())
                        .gender(gender)
                        .defaultAvatarUrl(avatar)
                        .role(NxvConstants.ROLE_USER)
                        .isEmailVerified(true)
                        .build());
                isNewUser = true;
            }
            oauthProvidersRepository.save(NxvOAuthProviders.builder()
                    .user(user)
                    .provider("GOOGLE")
                    .providerUserId(identity.subject())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException(ErrorCode.FORBIDDEN, "Tài khoản này đang bị khóa!");
        }
        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            user.setIsEmailVerified(true); // Google da xac minh email nay
        }
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        NxvCharacters character = isNewUser
                ? initNewPlayer(user, identity.name())
                : getOrCreateCharacter(user);
        return createSessionAndBuildResponse(user, character,
                req.getDeviceName() != null ? req.getDeviceName() : "Google OAuth2 Web");
    }

    // Chuc nang #5: Gui OTP & Xac thuc Email (TokenType = 0)
    @Transactional
    public Map<String, Object> sendEmailVerificationOtp(SendOtpRequest req) {
        return usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .map(user -> generateAndSaveOtp(user, 0, "Nếu email tồn tại, mã OTP xác thực đã được gửi."))
                .orElseGet(() -> genericOtpResponse(req.getEmail()));
    }

    @Transactional
    public Map<String, Object> verifyEmailOtp(VerifyEmailOtpRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, OTP_INVALID_MESSAGE));

        validateOtpToken(user.getUserId(), 0, req.getOtpCode());
        user.setIsEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        return Map.of("email", user.getEmail(), "isEmailVerified", true);
    }

    // Chuc nang #6: Quen mat khau & Dat lai mat khau qua OTP (TokenType = 1)
    @Transactional
    public Map<String, Object> forgotPasswordSendOtp(SendOtpRequest req) {
        return usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .map(user -> generateAndSaveOtp(user, 1, "Nếu email tồn tại, mã OTP khôi phục mật khẩu đã được gửi."))
                .orElseGet(() -> genericOtpResponse(req.getEmail()));
    }

    @Transactional
    public Map<String, Object> resetPasswordWithOtp(ResetPasswordRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, OTP_INVALID_MESSAGE));

        validateOtpToken(user.getUserId(), 1, req.getOtpCode());
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setIsLocked(false);
        user.setLoginFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
        userSessionsRepository.deactivateAllByUserId(user.getUserId()); // doi mat khau -> dang xuat moi thiet bi

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
        userSessionsRepository.deactivateAllByUserId(userId); // doi mat khau -> dang xuat moi thiet bi, phai dang nhap lai
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
    private static final String OTP_INVALID_MESSAGE = "Mã OTP không hợp lệ, đã hết hạn hoặc đã được sử dụng!";

    /** Phản hồi giống hệt khi email không tồn tại, để kẻ xấu không dò được email nào đã đăng ký. */
    private Map<String, Object> genericOtpResponse(String email) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("email", email.trim().toLowerCase());
        res.put("expiresInMinutes", 10);
        res.put("note", "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi.");
        return res;
    }

    private Map<String, Object> generateAndSaveOtp(Users user, int tokenType, String message) {
        // Chong spam: moi OTP cach nhau toi thieu 60 giay
        otpTokensRepository
                .findTopByUser_UserIdAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(user.getUserId(), tokenType)
                .filter(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(OTP_RESEND_COOLDOWN_SECONDS)))
                .ifPresent(t -> {
                    throw new AppException(ErrorCode.TOO_MANY_REQUESTS,
                            "Vui lòng đợi " + OTP_RESEND_COOLDOWN_SECONDS + " giây trước khi yêu cầu mã OTP mới!");
                });

        String rawOtp = String.format("%06d", RANDOM.nextInt(1000000));
        NxvOtpTokens otp = otpTokensRepository.save(NxvOtpTokens.builder()
                .user(user)
                .tokenType(tokenType)
                .tokenHash(passwordEncoder.encode(rawOtp))
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build());
        // TODO: gui rawOtp qua email bang JavaMailSender khi da cau hinh SMTP (spring.mail.*)

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("email", user.getEmail());
        res.put("expiresInMinutes", 10);
        res.put("note", message);
        if (otpDebugEnabled) {
            res.put("otpDebugCode", rawOtp); // CHI dung khi dev: test tren Swagger khong can SMTP
        }
        return res;
    }

    private void validateOtpToken(Long userId, int tokenType, String rawOtp) {
        NxvOtpTokens token = otpTokensRepository
                .findTopByUser_UserIdAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(userId, tokenType)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, OTP_INVALID_MESSAGE));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpFailedAttempts.remove(token.getTokenId());
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP đã hết hạn (quá 10 phút). Vui lòng yêu cầu mã mới!");
        }
        // Chong brute-force 6 chu so: sai qua 5 lan thi phai xin ma moi
        int attempts = otpFailedAttempts.getOrDefault(token.getTokenId(), 0);
        if (attempts >= OTP_MAX_ATTEMPTS) {
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS, "Nhập sai OTP quá nhiều lần. Vui lòng yêu cầu mã mới!");
        }
        if (!passwordEncoder.matches(rawOtp.trim(), token.getTokenHash())) {
            otpFailedAttempts.merge(token.getTokenId(), 1, Integer::sum);
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã OTP không chính xác!");
        }
        otpFailedAttempts.remove(token.getTokenId());
        token.setIsUsed(true);
        otpTokensRepository.save(token);
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (base.length() < 3) base = "hero" + base;
        if (base.length() > 40) base = base.substring(0, 40);
        String candidate = base;
        while (usersRepository.existsByUsername(candidate)) {
            candidate = base + "_" + (1000 + RANDOM.nextInt(9000));
        }
        return candidate;
    }

    /** Khoi tao nguoi choi moi: danh hieu khoi dau + nhan vat (Base 100 HP / 50 MP) + tui do 50 o. Dung chung cho dang ky va Google. */
    private NxvCharacters initNewPlayer(Users user, String preferredCharacterName) {
        NxvTitles starterTitle = titlesRepository.findFirstByTitleName(STARTER_TITLE_NAME).orElse(null);
        if (starterTitle != null) {
            userTitlesRepository.save(NxvUserTitles.builder()
                    .user(user)
                    .title(starterTitle)
                    .unlockedAt(LocalDateTime.now())
                    .build());
        }

        String charName = (preferredCharacterName != null && !preferredCharacterName.isBlank())
                ? preferredCharacterName.trim() : user.getUsername();
        if (charName.length() > 100) charName = charName.substring(0, 100);
        String avatar = user.getDefaultAvatarUrl() != null ? user.getDefaultAvatarUrl() : DEFAULT_MALE_AVATAR;

        NxvCharacters character = charactersRepository.save(NxvCharacters.builder()
                .user(user)
                .characterName(charName)
                .avatarUrl(avatar)
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
                .build());

        if (!inventoryConfigRepository.existsById(user.getUserId())) {
            inventoryConfigRepository.save(NxvUserInventoryConfig.builder()
                    .userId(user.getUserId())
                    .maxSlots(50)
                    .build());
        }
        return character;
    }

    private NxvCharacters getOrCreateCharacter(Users user) {
        return charactersRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> initNewPlayer(user, null));
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