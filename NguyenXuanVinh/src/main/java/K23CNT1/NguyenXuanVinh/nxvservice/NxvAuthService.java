package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvAuthDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvJwtUtils;
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
            throw new AppException(ErrorCode.BAD_REQUEST, "Username Ä‘Ã£ tá»“n táº¡i trong há»‡ thá»‘ng!");
        }
        if (usersRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Email Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng!");
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
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "TÃ i khoáº£n hoáº·c máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c!"));

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException(ErrorCode.FORBIDDEN, "TÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a do nháº­p sai máº­t kháº©u quÃ¡ 5 láº§n! Vui lÃ²ng dÃ¹ng tÃ­nh nÄƒng QuÃªn máº­t kháº©u Ä‘á»ƒ má»Ÿ khÃ³a.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            int fails = loginAttemptService.recordFailedLogin(user.getUserId());
            int remaining = Math.max(0, NxvLoginAttemptService.MAX_FAIL_ATTEMPTS - fails);
            if (remaining == 0) {
                throw new AppException(ErrorCode.FORBIDDEN, "Báº¡n Ä‘Ã£ nháº­p sai 5 láº§n liÃªn tiáº¿p. TÃ i khoáº£n Ä‘Ã£ bá»‹ khÃ³a tá»± Ä‘á»™ng!");
            }
            throw new AppException(ErrorCode.UNAUTHORIZED, "Máº­t kháº©u khÃ´ng chÃ­nh xÃ¡c! CÃ²n " + remaining + " láº§n thá»­ trÆ°á»›c khi bá»‹ khÃ³a.");
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
            throw new AppException(ErrorCode.FORBIDDEN, "TÃ i khoáº£n nÃ y Ä‘ang bá»‹ khÃ³a!");
        }

        NxvCharacters character = getOrCreateCharacter(user);
        return createSessionAndBuildResponse(user, character,
                req.getDeviceName() != null ? req.getDeviceName() : "Google OAuth2 Web");
    }

    // Chuc nang #5: Gui OTP & Xac thuc Email (TokenType = 0)
    @Transactional
    public Map<String, Object> sendEmailVerificationOtp(SendOtpRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i Email nÃ y!"));
        return generateAndSaveOtp(user, 0, "MÃ£ OTP xÃ¡c thá»±c tÃ i khoáº£n Ä‘Ã£ Ä‘Æ°á»£c khá»Ÿi táº¡o!");
    }

    @Transactional
    public Map<String, Object> verifyEmailOtp(VerifyEmailOtpRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i Email nÃ y!"));

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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i Email nÃ y!"));
        return generateAndSaveOtp(user, 1, "MÃ£ OTP khÃ´i phá»¥c máº­t kháº©u Ä‘Ã£ Ä‘Æ°á»£c khá»Ÿi táº¡o!");
    }

    @Transactional
    public Map<String, Object> resetPasswordWithOtp(ResetPasswordRequest req) {
        Users user = usersRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y tÃ i khoáº£n vá»›i Email nÃ y!"));

        validateOtpToken(user.getUserId(), 1, req.getOtpCode());
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setIsLocked(false);
        user.setLoginFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        return Map.of("email", user.getEmail(), "status", "ÄÃ£ Ä‘áº·t láº¡i máº­t kháº©u vÃ  má»Ÿ khÃ³a tÃ i khoáº£n thÃ nh cÃ´ng!");
    }

    // Chuc nang #7: Thay doi mat khau
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        if (user.getPasswordHash() != null && !passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Máº­t kháº©u hiá»‡n táº¡i khÃ´ng chÃ­nh xÃ¡c!");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
    }

    // Chuc nang #8: Xem & Cap nhat Profile ca nhan
    @Transactional(readOnly = true)
    public AuthResponse getMyProfile(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        NxvCharacters character = getOrCreateCharacter(user);
        return buildAuthResponse(user, character, null, null);
    }

    @Transactional
    public AuthResponse updateMyProfile(Long userId, UpdateProfileRequest req) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y phiÃªn Ä‘Äƒng nháº­p nÃ y!"));
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
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "KhÃ´ng tÃ¬m tháº¥y mÃ£ OTP há»£p lá»‡ hoáº·c mÃ£ Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng!"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "MÃ£ OTP Ä‘Ã£ háº¿t háº¡n (quÃ¡ 10 phÃºt). Vui lÃ²ng yÃªu cáº§u mÃ£ má»›i!");
        }
        if (!passwordEncoder.matches(rawOtp.trim(), token.getTokenHash())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "MÃ£ OTP khÃ´ng chÃ­nh xÃ¡c!");
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