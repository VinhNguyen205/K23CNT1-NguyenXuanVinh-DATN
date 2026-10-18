# ==============================================================================
# TASKTALE / LIFERPG - TASK 0 AUTOMATED GENERATOR (MASTER VERSION 2.2)
# Dong bo 100% voi NxvTaskTaleDB v2.2: 54 Bang, 5 Views, 7 Stored Procedures
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
if (-not (Test-Path $javaRoot)) {
    Write-Error "Khong tim thay thu muc src\main\java. Hay chay script tai thu muc goc chua pom.xml!"
    exit 1
}

$entityDirObj = Get-ChildItem -Path $javaRoot -Recurse -Directory -Filter "nxventity" | Select-Object -First 1
if ($null -eq $entityDirObj) {
    Write-Error "Khong tim thay package nxventity trong src\main\java!"
    exit 1
}

$baseDir = $entityDirObj.Parent.FullName
$relPath = $baseDir.Substring($javaRoot.Length).TrimStart('\', '/')
$basePkg = $relPath -replace '[\\/]', '.'

Write-Host "==> Base Package: $basePkg" -ForegroundColor Cyan

# ==============================================================================
# 1. SINH PACKAGE nxvenums (NxvConstants & ErrorCode day du ma loi v2.2)
# ==============================================================================
Write-JavaFile "$baseDir\nxvenums\NxvConstants.java" @"
package $basePkg.nxvenums;

public final class NxvConstants {
    private NxvConstants() {}

    // Users.Gender
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;

    // Users.Role
    public static final int ROLE_USER = 0;
    public static final int ROLE_MODERATOR = 1;
    public static final int ROLE_SUPER_ADMIN = 2;

    // NxvTasks.Status
    public static final int TASK_PENDING = 0;
    public static final int TASK_COMPLETED = 1;
    public static final int TASK_FAILED = 2;
    public static final int TASK_SKIPPED = 3;

    // NxvTasks.Difficulty
    public static final int DIFF_EASY = 1;
    public static final int DIFF_NORMAL = 2;
    public static final int DIFF_HARD = 3;

    // NxvUserPets.Stage
    public static final int PET_STAGE_EGG = 0;
    public static final int PET_STAGE_BABY = 1;
    public static final int PET_STAGE_ADULT = 2;

    // NxvItems.ItemType
    public static final int ITEM_EQUIPMENT = 0;
    public static final int ITEM_CONSUMABLE = 1;
    public static final int ITEM_AVATAR = 2;
    public static final int ITEM_AVATAR_FRAME = 3;
    public static final int ITEM_PET_EGG = 4;
    public static final int ITEM_TITLE = 5;
}
"@

Write-JavaFile "$baseDir\nxvenums\ErrorCode.java" @"
package $basePkg.nxvenums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SUCCESS(200, "Thao tÃ¡c thÃ nh cÃ´ng", HttpStatus.OK),
    BAD_REQUEST(400, "YÃªu cáº§u khÃ´ng há»£p lá»‡", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "ChÆ°a xÃ¡c thá»±c hoáº·c phiÃªn Ä‘Äƒng nháº­p háº¿t háº¡n", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "Báº¡n khÃ´ng cÃ³ quyá»n thá»±c hiá»‡n thao tÃ¡c nÃ y", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "KhÃ´ng tÃ¬m tháº¥y dá»¯ liá»‡u yÃªu cáº§u", HttpStatus.NOT_FOUND),

    // Stored Procedure Error Codes (v2.2)
    TASK_NOT_FOUND(50010, "Task khÃ´ng tá»“n táº¡i.", HttpStatus.NOT_FOUND),
    TASK_ALREADY_PROCESSED(50011, "Task Ä‘Ã£ xá»­ lÃ½ rá»“i.", HttpStatus.BAD_REQUEST),
    CHARACTER_DEAD(50012, "NhÃ¢n váº­t Ä‘ang cháº¿t, khÃ´ng thá»ƒ lÃ m viá»‡c.", HttpStatus.FORBIDDEN),
    ITEM_NOT_EQUIPMENT(50100, "Váº­t pháº©m khÃ´ng pháº£i trang bá»‹", HttpStatus.BAD_REQUEST),
    ITEM_NOT_OWNED(50101, "KhÃ´ng sá»Ÿ há»¯u váº­t pháº©m nÃ y", HttpStatus.BAD_REQUEST),
    ITEM_NOT_CONSUMABLE(50102, "KhÃ´ng pháº£i váº­t pháº©m tiÃªu hao", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_ITEM_QTY(50103, "KhÃ´ng Ä‘á»§ váº­t pháº©m trong tÃºi Ä‘á»“", HttpStatus.BAD_REQUEST),
    SLOT_EMPTY(50104, "Slot nÃ y hiá»‡n khÃ´ng cÃ³ trang bá»‹ nÃ o", HttpStatus.BAD_REQUEST),
    DEAD_CANNOT_FOCUS(50200, "NhÃ¢n váº­t Ä‘ang cháº¿t, khÃ´ng thá»ƒ tÃ­ch lÅ©y thá»i gian táº­p trung.", HttpStatus.FORBIDDEN),

    INSUFFICIENT_GOLD(4001, "KhÃ´ng Ä‘á»§ VÃ ng (Gold) Ä‘á»ƒ thá»±c hiá»‡n giao dá»‹ch", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_MP(4002, "KhÃ´ng Ä‘á»§ NÄƒng lÆ°á»£ng (MP) Ä‘á»ƒ kÃ­ch hoáº¡t ká»¹ nÄƒng", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(500, "Lá»—i há»‡ thá»‘ng ná»™i bá»™", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
"@

# ==============================================================================
# 2. SINH PACKAGE nxvdto & nxvexception
# ==============================================================================
Write-JavaFile "$baseDir\nxvdto\ApiResponse.java" @"
package $basePkg.nxvdto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    private int code = 200;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("ThÃ nh cÃ´ng")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
"@

Write-JavaFile "$baseDir\nxvexception\AppException.java" @"
package $basePkg.nxvexception;

import $basePkg.nxvenums.ErrorCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
"@

Write-JavaFile "$baseDir\nxvexception\GlobalExceptionHandler.java" @"
package $basePkg.nxvexception;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvenums.ErrorCode;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode ec = ex.getErrorCode();
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.error(ec.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseException(DataAccessException ex) {
        Throwable root = ex.getMostSpecificCause();
        if (root instanceof SQLException sqlEx) {
            int errNum = sqlEx.getErrorCode();
            if (errNum >= 50000 && errNum <= 50999) {
                HttpStatus status = (errNum == 50012 || errNum == 50200) ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status)
                        .body(ApiResponse.error(errNum, sqlEx.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Lá»—i dá»¯ liá»‡u SQL: " + root.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(400, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, ex.getMessage()));
    }
}
"@

# ==============================================================================
# 3. HAM HELPER SINH ENTITY VA REPOSITORY
# ==============================================================================
function New-NxvEntity {
    param(
        [string]$ClassName,
        [string]$TableName,
        [string]$PkType,
        [string]$Body,
        [bool]$Immutable = $false
    )
    $immImport = if ($Immutable) { "import org.hibernate.annotations.Immutable;`n" } else { "" }
    $immAnnot  = if ($Immutable) { "@Immutable`n" } else { "" }

    $entityContent = @"
package $basePkg.nxventity;

import jakarta.persistence.*;
import lombok.*;
${immImport}import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
$immAnnot@Table(name = "$TableName")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class $ClassName {
$Body
}
"@
    Write-JavaFile "$baseDir\nxventity\$ClassName.java" $entityContent

    $repoName = "${ClassName}Repository"
    $repoContent = @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.$ClassName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface $repoName extends JpaRepository<$ClassName, $PkType> {
}
"@
    Write-JavaFile "$baseDir\nxvrepository\$repoName.java" $repoContent
}

# ==============================================================================
# 4. SINH DU 54 ENTITIES CHO 54 BANG TRONG NxvTaskTaleDB v2.2
# ==============================================================================

# 1. Users (BAT BUOC GIU CHU s)
New-NxvEntity "Users" "Users" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "PasswordHash", length = 256)
    private String passwordHash;

    @Builder.Default
    @Column(name = "Gender", nullable = false)
    private Integer gender = 0;

    @Column(name = "DefaultAvatarUrl", length = 500)
    private String defaultAvatarUrl;

    @Builder.Default
    @Column(name = "Role", nullable = false)
    private Integer role = 0;

    @Builder.Default
    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean isEmailVerified = false;

    @Builder.Default
    @Column(name = "IsLocked", nullable = false)
    private Boolean isLocked = false;

    @Builder.Default
    @Column(name = "IsDead", nullable = false)
    private Boolean isDead = false;

    @Builder.Default
    @Column(name = "LoginFailCount", nullable = false)
    private Integer loginFailCount = 0;

    @Column(name = "LastLoginAt")
    private LocalDateTime lastLoginAt;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;
"@

# 2. NxvLevelConfig (PK Level - KHONG IDENTITY)
New-NxvEntity "NxvLevelConfig" "NxvLevelConfig" "Integer" @"
    @Id
    @Column(name = "Level")
    private Integer level;

    @Column(name = "EXPRequired", nullable = false)
    private Long expRequired;

    @Builder.Default
    @Column(name = "MaxHP", nullable = false)
    private Integer maxHp = 100;

    @Builder.Default
    @Column(name = "MaxMP", nullable = false)
    private Integer maxMp = 50;
"@

# 3. NxvTags
New-NxvEntity "NxvTags" "NxvTags" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TagID")
    private Integer tagId;

    @Column(name = "TagName", nullable = false, unique = true, length = 50)
    private String tagName;

    @Builder.Default
    @Column(name = "ColorHex", nullable = false, length = 7)
    private String colorHex = "#FFFFFF";
"@

# 4. NxvEquipmentSlots
New-NxvEntity "NxvEquipmentSlots" "NxvEquipmentSlots" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SlotID")
    private Integer slotId;

    @Column(name = "SlotName", nullable = false, length = 50)
    private String slotName;

    @Column(name = "SlotKey", nullable = false, unique = true, length = 30)
    private String slotKey;
"@

# 5. NxvClasses
New-NxvEntity "NxvClasses" "NxvClasses" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassID")
    private Integer classId;

    @Column(name = "ClassName", nullable = false, length = 100)
    private String className;

    @Builder.Default
    @Column(name = "PassiveEXPBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveExpBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "PassiveGoldBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveGoldBonus = BigDecimal.ZERO;
"@

# 6. NxvPetSpecies
New-NxvEntity "NxvPetSpecies" "NxvPetSpecies" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SpeciesID")
    private Integer speciesId;

    @Column(name = "SpeciesName", nullable = false, length = 100)
    private String speciesName;

    @Builder.Default
    @Column(name = "ElementType", nullable = false, length = 50)
    private String elementType = "Tá»± nhiÃªn";

    @Column(name = "EggImageUrl", length = 500)
    private String eggImageUrl;

    @Column(name = "BabyImageUrl", length = 500)
    private String babyImageUrl;

    @Column(name = "AdultImageUrl", length = 500)
    private String adultImageUrl;

    @Builder.Default
    @Column(name = "EvolveDays", nullable = false)
    private Integer evolveDays = 14;

    @Builder.Default
    @Column(name = "PassiveGoldBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveGoldBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "PassiveDropRateBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal passiveDropRateBonus = BigDecimal.ZERO;
"@

# 7. NxvTitles (V2.1+)
New-NxvEntity "NxvTitles" "NxvTitles" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TitleID")
    private Integer titleId;

    @Column(name = "TitleName", nullable = false, length = 100)
    private String titleName;

    @Column(name = "TitleIcon", length = 500)
    private String titleIcon;

    @Builder.Default
    @Column(name = "IsExclusive", nullable = false)
    private Boolean isExclusive = false;
"@

# 8. NxvOAuthProviders
New-NxvEntity "NxvOAuthProviders" "NxvOAuthProviders" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OAuthID")
    private Long oauthId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "Provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "ProviderUserID", nullable = false, length = 200)
    private String providerUserId;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 9. NxvOtpTokens
New-NxvEntity "NxvOtpTokens" "NxvOtpTokens" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TokenID")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "TokenType", nullable = false)
    private Integer tokenType;

    @Column(name = "TokenHash", nullable = false, length = 256)
    private String tokenHash;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "IsUsed", nullable = false)
    private Boolean isUsed = false;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 10. NxvUserSessions
New-NxvEntity "NxvUserSessions" "NxvUserSessions" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SessionID")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "DeviceName", length = 200)
    private String deviceName;

    @Column(name = "DeviceToken", nullable = false, length = 500)
    private String deviceToken;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "LastActiveAt", nullable = false)
    private LocalDateTime lastActiveAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 11. NxvCharacters (V2.2: Co BaseMaxHP, BaseMaxMP, CurrentTitleID -> NxvTitles)
New-NxvEntity "NxvCharacters" "NxvCharacters" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CharacterID")
    private Long characterId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    private Users user;

    @Column(name = "CharacterName", nullable = false, length = 100)
    private String characterName;

    @Column(name = "AvatarUrl", length = 500)
    private String avatarUrl;

    @Column(name = "AvatarFrameUrl", length = 500)
    private String avatarFrameUrl;

    @Builder.Default
    @Column(name = "CurrentLevel", nullable = false)
    private Integer currentLevel = 1;

    @Builder.Default
    @Column(name = "CurrentEXP", nullable = false)
    private Long currentExp = 0L;

    @Builder.Default
    @Column(name = "EXPToNextLevel", nullable = false)
    private Long expToNextLevel = 100L;

    @Builder.Default
    @Column(name = "BaseMaxHP", nullable = false)
    private Integer baseMaxHp = 100;

    @Builder.Default
    @Column(name = "BaseMaxMP", nullable = false)
    private Integer baseMaxMp = 50;

    @Builder.Default
    @Column(name = "MaxHP", nullable = false)
    private Integer maxHp = 100;

    @Builder.Default
    @Column(name = "CurrentHP", nullable = false)
    private Integer currentHp = 100;

    @Builder.Default
    @Column(name = "MaxMP", nullable = false)
    private Integer maxMp = 50;

    @Builder.Default
    @Column(name = "CurrentMP", nullable = false)
    private Integer currentMp = 50;

    @Builder.Default
    @Column(name = "Gold", nullable = false)
    private Long gold = 0L;

    @Builder.Default
    @Column(name = "SkillPoints", nullable = false)
    private Integer skillPoints = 0;

    @Builder.Default
    @Column(name = "IsShieldActive", nullable = false)
    private Boolean isShieldActive = false;

    @Builder.Default
    @Column(name = "TotalTasksDone", nullable = false)
    private Integer totalTasksDone = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CurrentClassID")
    private NxvClasses currentClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CurrentTitleID")
    private NxvTitles currentTitle;

    @Builder.Default
    @Column(name = "StreakDays", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "LastCheckInDate")
    private LocalDate lastCheckInDate;

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
"@

# 12. NxvUserTitles (V2.1+)
New-NxvEntity "NxvUserTitles" "NxvUserTitles" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserTitleID")
    private Long userTitleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TitleID", nullable = false)
    private NxvTitles title;

    @Builder.Default
    @Column(name = "UnlockedAt", nullable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();
"@

# 13. NxvStatLogs
New-NxvEntity "NxvStatLogs" "NxvStatLogs" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "LogType", nullable = false, length = 30)
    private String logType;

    @Column(name = "Delta", nullable = false)
    private Long delta;

    @Column(name = "SourceType", length = 50)
    private String sourceType;

    @Column(name = "SourceID")
    private Long sourceId;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 14. NxvParties
New-NxvEntity "NxvParties" "NxvParties" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PartyID")
    private Long partyId;

    @Column(name = "PartyName", nullable = false, length = 100)
    private String partyName;

    @Column(name = "Description", length = 300)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LeaderUserID", nullable = false)
    private Users leaderUser;

    @Builder.Default
    @Column(name = "MaxMembers", nullable = false)
    private Integer maxMembers = 5;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 15. NxvPartyMembers
New-NxvEntity "NxvPartyMembers" "NxvPartyMembers" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PartyMemberID")
    private Long partyMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyID", nullable = false)
    private NxvParties party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Builder.Default
    @Column(name = "Role", nullable = false)
    private Integer role = 0;

    @Builder.Default
    @Column(name = "JoinedAt", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();
"@

# 16. NxvPartyMessages
New-NxvEntity "NxvPartyMessages" "NxvPartyMessages" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageID")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyID", nullable = false)
    private NxvParties party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SenderUserID", nullable = false)
    private Users senderUser;

    @Column(name = "Content", nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 17. NxvFriendships
New-NxvEntity "NxvFriendships" "NxvFriendships" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FriendshipID")
    private Long friendshipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequesterID", nullable = false)
    private Users requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AddresseeID", nullable = false)
    private Users addressee;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 18. NxvEpicQuests
New-NxvEntity "NxvEpicQuests" "NxvEpicQuests" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EpicQuestID")
    private Long epicQuestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyID")
    private NxvParties party;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Builder.Default
    @Column(name = "IsGPAQuest", nullable = false)
    private Boolean isGpaQuest = false;

    @Column(name = "TargetGPA", precision = 4, scale = 2)
    private BigDecimal targetGpa;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 19. NxvTasks
New-NxvEntity "NxvTasks" "NxvTasks" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TaskID")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpicQuestID")
    private NxvEpicQuests epicQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PartyTaskID")
    private NxvTasks partyTask;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "DescriptionMarkdown", columnDefinition = "NVARCHAR(MAX)")
    private String descriptionMarkdown;

    @Column(name = "ColorLabel", length = 7)
    private String colorLabel;

    @Builder.Default
    @Column(name = "Difficulty", nullable = false)
    private Integer difficulty = 1;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "RewardMP", nullable = false)
    private Integer rewardMp = 0;

    @Builder.Default
    @Column(name = "PenaltyHP", nullable = false)
    private Integer penaltyHp = 0;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "SortOrder", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "DeadlineAt")
    private LocalDateTime deadlineAt;

    @Column(name = "ReminderBeforeMins")
    private Integer reminderBeforeMins;

    @Builder.Default
    @Column(name = "IsFrozen", nullable = false)
    private Boolean isFrozen = false;

    @Builder.Default
    @Column(name = "IsShielded", nullable = false)
    private Boolean isShielded = false;

    @Builder.Default
    @Column(name = "UndoAvailable", nullable = false)
    private Boolean undoAvailable = false;

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
"@

# 20. NxvTaskTemplates
New-NxvEntity "NxvTaskTemplates" "NxvTaskTemplates" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TemplateID")
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Builder.Default
    @Column(name = "Difficulty", nullable = false)
    private Integer difficulty = 1;

    @Column(name = "RecurringRule", nullable = false, length = 100)
    private String recurringRule;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
"@

# 21. NxvSubTasks
New-NxvEntity "NxvSubTasks" "NxvSubTasks" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubTaskID")
    private Long subTaskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpicQuestID", nullable = false)
    private NxvEpicQuests epicQuest;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Builder.Default
    @Column(name = "IsDone", nullable = false)
    private Boolean isDone = false;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 22. NxvTaskTags
New-NxvEntity "NxvTaskTags" "NxvTaskTags" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TaskTagID")
    private Long taskTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TaskID", nullable = false)
    private NxvTasks task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TagID", nullable = false)
    private NxvTags tag;
"@

# 23. NxvGPASubjects
New-NxvEntity "NxvGPASubjects" "NxvGPASubjects" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubjectID")
    private Long subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpicQuestID", nullable = false)
    private NxvEpicQuests epicQuest;

    @Column(name = "SubjectName", nullable = false, length = 200)
    private String subjectName;

    @Builder.Default
    @Column(name = "Credits", nullable = false)
    private Integer credits = 3;

    @Column(name = "Score", precision = 4, scale = 2)
    private BigDecimal score;

    @Builder.Default
    @Column(name = "IsExcluded", nullable = false)
    private Boolean isExcluded = false;

    @Column(name = "ExcludeReason", length = 100)
    private String excludeReason;
"@

# 24. NxvTaskHistory
New-NxvEntity "NxvTaskHistory" "NxvTaskHistory" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HistoryID")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TaskID", nullable = false)
    private NxvTasks task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "ActionType", nullable = false, length = 20)
    private String actionType;

    @Builder.Default
    @Column(name = "ActionAt", nullable = false)
    private LocalDateTime actionAt = LocalDateTime.now();
"@

# 25. NxvUserUnlockedClasses
New-NxvEntity "NxvUserUnlockedClasses" "NxvUserUnlockedClasses" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UnlockID")
    private Long unlockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassID", nullable = false)
    private NxvClasses nxvClass;

    @Builder.Default
    @Column(name = "UnlockedAt", nullable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();
"@

# 26. NxvClassChangeLogs
New-NxvEntity "NxvClassChangeLogs" "NxvClassChangeLogs" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FromClassID")
    private NxvClasses fromClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ToClassID", nullable = false)
    private NxvClasses toClass;

    @Builder.Default
    @Column(name = "ChangedAt", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
"@

# 27. NxvSkillDefinitions
New-NxvEntity "NxvSkillDefinitions" "NxvSkillDefinitions" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SkillID")
    private Integer skillId;

    @Column(name = "SkillName", nullable = false, length = 100)
    private String skillName;

    @Builder.Default
    @Column(name = "SkillType", nullable = false)
    private Integer skillType = 0;

    @Builder.Default
    @Column(name = "MPCost", nullable = false)
    private Integer mpCost = 0;

    @Builder.Default
    @Column(name = "CooldownSeconds", nullable = false)
    private Integer cooldownSeconds = 0;

    @Builder.Default
    @Column(name = "RequiredLevel", nullable = false)
    private Integer requiredLevel = 1;

    @Builder.Default
    @Column(name = "MaxSkillLevel", nullable = false)
    private Integer maxSkillLevel = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentSkillID")
    private NxvSkillDefinitions parentSkill;

    @Column(name = "SkillEffect", length = 50)
    private String skillEffect;
"@

# 28. NxvUserSkills
New-NxvEntity "NxvUserSkills" "NxvUserSkills" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserSkillID")
    private Long userSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SkillID", nullable = false)
    private NxvSkillDefinitions skill;

    @Builder.Default
    @Column(name = "IsUnlocked", nullable = false)
    private Boolean isUnlocked = false;

    @Builder.Default
    @Column(name = "SkillLevel", nullable = false)
    private Integer skillLevel = 1;

    @Column(name = "CooldownEndsAt")
    private LocalDateTime cooldownEndsAt;
"@

# 29. NxvSkillUsageLogs
New-NxvEntity "NxvSkillUsageLogs" "NxvSkillUsageLogs" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsageID")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SkillID", nullable = false)
    private NxvSkillDefinitions skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetTaskID")
    private NxvTasks targetTask;

    @Builder.Default
    @Column(name = "UsedAt", nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();
"@

# 30. NxvUserTagMastery
New-NxvEntity "NxvUserTagMastery" "NxvUserTagMastery" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MasteryID")
    private Long masteryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TagID", nullable = false)
    private NxvTags tag;

    @Builder.Default
    @Column(name = "TaskCount", nullable = false)
    private Integer taskCount = 0;

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
"@

# 31. NxvUserPets
New-NxvEntity "NxvUserPets" "NxvUserPets" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserPetID")
    private Long userPetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SpeciesID", nullable = false)
    private NxvPetSpecies species;

    @Builder.Default
    @Column(name = "PetName", nullable = false, length = 100)
    private String petName = "ThÃº CÆ°ng";

    @Column(name = "ElementType", length = 50)
    private String elementType;

    @Builder.Default
    @Column(name = "Stage", nullable = false)
    private Integer stage = 0;

    @Column(name = "CurrentImageUrl", length = 500)
    private String currentImageUrl;

    @Builder.Default
    @Column(name = "Hunger", nullable = false)
    private Integer hunger = 100;

    @Builder.Default
    @Column(name = "Affection", nullable = false)
    private Integer affection = 50;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = false;

    @Builder.Default
    @Column(name = "IsHibernating", nullable = false)
    private Boolean isHibernating = false;

    @Column(name = "HatchedAt")
    private LocalDateTime hatchedAt;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 32. NxvPetInteractionLogs
New-NxvEntity "NxvPetInteractionLogs" "NxvPetInteractionLogs" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserPetID", nullable = false)
    private NxvUserPets userPet;

    @Column(name = "InteractionType", nullable = false, length = 30)
    private String interactionType;

    @Column(name = "UsedItemID")
    private Integer usedItemId;

    @Column(name = "AIMessage", length = 1000)
    private String aiMessage;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 33. NxvPetDex
New-NxvEntity "NxvPetDex" "NxvPetDex" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PetDexID")
    private Long petDexId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SpeciesID", nullable = false)
    private NxvPetSpecies species;

    @Builder.Default
    @Column(name = "FirstObtainedAt", nullable = false)
    private LocalDateTime firstObtainedAt = LocalDateTime.now();
"@

# 34. NxvItems
New-NxvEntity "NxvItems" "NxvItems" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ItemID")
    private Integer itemId;

    @Column(name = "ItemName", nullable = false, length = 200)
    private String itemName;

    @Column(name = "ItemType", nullable = false)
    private Integer itemType;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EquipSlotID")
    private NxvEquipmentSlots equipSlot;

    @Builder.Default
    @Column(name = "BuffMaxHP", nullable = false)
    private Integer buffMaxHp = 0;

    @Builder.Default
    @Column(name = "BuffMaxMP", nullable = false)
    private Integer buffMaxMp = 0;

    @Builder.Default
    @Column(name = "BuffEXPBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal buffExpBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "BuffGoldBonus", nullable = false, precision = 5, scale = 4)
    private BigDecimal buffGoldBonus = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "BuffDeadlineDmgReduce", nullable = false, precision = 5, scale = 4)
    private BigDecimal buffDeadlineDmgReduce = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "HealHP", nullable = false)
    private Integer healHp = 0;

    @Builder.Default
    @Column(name = "SellPrice", nullable = false)
    private Integer sellPrice = 0;
"@

# 35. NxvUserInventoryConfig (PK UserID - KHONG IDENTITY)
New-NxvEntity "NxvUserInventoryConfig" "NxvUserInventoryConfig" "Long" @"
    @Id
    @Column(name = "UserID")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", insertable = false, updatable = false)
    private Users user;

    @Builder.Default
    @Column(name = "MaxSlots", nullable = false)
    private Integer maxSlots = 50;
"@

# 36. NxvInventory
New-NxvEntity "NxvInventory" "NxvInventory" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryID")
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Builder.Default
    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;
"@

# 37. NxvEquippedItems
New-NxvEntity "NxvEquippedItems" "NxvEquippedItems" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EquippedID")
    private Long equippedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SlotID", nullable = false)
    private NxvEquipmentSlots slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;
"@

# 38. NxvGachaBanners
New-NxvEntity "NxvGachaBanners" "NxvGachaBanners" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BannerID")
    private Integer bannerId;

    @Builder.Default
    @Column(name = "BannerName", nullable = false, length = 150)
    private String bannerName = "Há»™p MÃ¹ BÃ­ áº¨n";

    @Column(name = "BannerImageUrl", length = 500)
    private String bannerImageUrl;

    @Builder.Default
    @Column(name = "CostPerPull", nullable = false)
    private Integer costPerPull = 100;

    @Builder.Default
    @Column(name = "PityLimit", nullable = false)
    private Integer pityLimit = 50;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
"@

# 39. NxvGachaDropRates
New-NxvEntity "NxvGachaDropRates" "NxvGachaDropRates" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DropRateID")
    private Integer dropRateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BannerID", nullable = false)
    private NxvGachaBanners banner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Column(name = "DropRate", nullable = false, precision = 8, scale = 6)
    private BigDecimal dropRate;

    @Builder.Default
    @Column(name = "IsFeatured", nullable = false)
    private Boolean isFeatured = false;
"@

# 40. NxvUserPityCounters
New-NxvEntity "NxvUserPityCounters" "NxvUserPityCounters" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PityID")
    private Long pityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BannerID", nullable = false)
    private NxvGachaBanners banner;

    @Builder.Default
    @Column(name = "PullCount", nullable = false)
    private Integer pullCount = 0;

    @Builder.Default
    @Column(name = "TotalPulls", nullable = false)
    private Integer totalPulls = 0;
"@

# 41. NxvGachaHistory
New-NxvEntity "NxvGachaHistory" "NxvGachaHistory" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GachaHistoryID")
    private Long gachaHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BannerID", nullable = false)
    private NxvGachaBanners banner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Builder.Default
    @Column(name = "IsPity", nullable = false)
    private Boolean isPity = false;

    @Builder.Default
    @Column(name = "GoldSpent", nullable = false)
    private Integer goldSpent = 0;

    @Builder.Default
    @Column(name = "PulledAt", nullable = false)
    private LocalDateTime pulledAt = LocalDateTime.now();
"@

# 42. NxvMarketListings
New-NxvEntity "NxvMarketListings" "NxvMarketListings" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ListingID")
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SellerUserID", nullable = false)
    private Users sellerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ItemID", nullable = false)
    private NxvItems item;

    @Builder.Default
    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "PricePerUnit", nullable = false)
    private Integer pricePerUnit;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "TaxRate", nullable = false, precision = 5, scale = 4)
    private BigDecimal taxRate = new BigDecimal("0.0500");

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "SoldAt")
    private LocalDateTime soldAt;
"@

# 43. NxvMarketTransactions
New-NxvEntity "NxvMarketTransactions" "NxvMarketTransactions" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MarketTxID")
    private Long marketTxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ListingID", nullable = false)
    private NxvMarketListings listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BuyerUserID", nullable = false)
    private Users buyerUser;

    @Column(name = "TotalPrice", nullable = false)
    private Long totalPrice;

    @Builder.Default
    @Column(name = "TxAt", nullable = false)
    private LocalDateTime txAt = LocalDateTime.now();
"@

# 44. NxvAchievements (V2.1+)
New-NxvEntity "NxvAchievements" "NxvAchievements" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AchievementID")
    private Integer achievementId;

    @Column(name = "Code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "AchievementName", nullable = false, length = 150)
    private String achievementName;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "IconUrl", length = 500)
    private String iconUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RewardTitleID")
    private NxvTitles rewardTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RewardItemID")
    private NxvItems rewardItem;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;
"@

# 45. NxvUserAchievements (V2.1+)
New-NxvEntity "NxvUserAchievements" "NxvUserAchievements" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserAchievementID")
    private Long userAchievementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AchievementID", nullable = false)
    private NxvAchievements achievement;

    @Builder.Default
    @Column(name = "UnlockedAt", nullable = false)
    private LocalDateTime unlockedAt = LocalDateTime.now();
"@

# 46. NxvNotifications
New-NxvEntity "NxvNotifications" "NxvNotifications" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private Users user;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Content", nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @Column(name = "NotificationType", nullable = false, length = 30)
    private String notificationType = "SYSTEM";

    @Builder.Default
    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 47. NxvJournals
New-NxvEntity "NxvJournals" "NxvJournals" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JournalID")
    private Long journalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "MoodTag", length = 50)
    private String moodTag;

    @Column(name = "AIResponse", length = 1000)
    private String aiResponse;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 48. NxvFocusLogs
New-NxvEntity "NxvFocusLogs" "NxvFocusLogs" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FocusLogID")
    private Long focusLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "DurationMinutes", nullable = false)
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "RewardEXP", nullable = false)
    private Integer rewardExp = 0;

    @Builder.Default
    @Column(name = "RewardGold", nullable = false)
    private Integer rewardGold = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 49. NxvUserReports
New-NxvEntity "NxvUserReports" "NxvUserReports" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReportID")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReporterUserID", nullable = false)
    private Users reporterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TargetUserID")
    private Users targetUser;

    @Column(name = "ReportType", nullable = false, length = 50)
    private String reportType;

    @Column(name = "Description", nullable = false, length = 2000)
    private String description;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 50. NxvAIConfig
New-NxvEntity "NxvAIConfig" "NxvAIConfig" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConfigID")
    private Integer configId;

    @Column(name = "Provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "ModelName", nullable = false, length = 100)
    private String modelName;

    @Column(name = "APIKeyEncrypted", nullable = false, length = 1000)
    private String apiKeyEncrypted;

    @Column(name = "SystemPrompt", columnDefinition = "NVARCHAR(MAX)")
    private String systemPrompt;

    @Builder.Default
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
"@

# 51. NxvRevivalPackages
New-NxvEntity "NxvRevivalPackages" "NxvRevivalPackages" "Integer" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PackageID")
    private Integer packageId;

    @Column(name = "PackageName", nullable = false, length = 100)
    private String packageName;

    @Column(name = "AmountVND", nullable = false)
    private Integer amountVnd;
"@

# 52. NxvPaymentOrders
New-NxvEntity "NxvPaymentOrders" "NxvPaymentOrders" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "OrderCode", nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(name = "AmountVND", nullable = false)
    private Integer amountVnd;

    @Builder.Default
    @Column(name = "Status", nullable = false)
    private Integer status = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 53. NxvWebhookLogs
New-NxvEntity "NxvWebhookLogs" "NxvWebhookLogs" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WebhookLogID")
    private Long webhookLogId;

    @Column(name = "OrderCode", nullable = false, length = 50)
    private String orderCode;

    @Column(name = "PayloadJson", columnDefinition = "NVARCHAR(MAX)")
    private String payloadJson;

    @Column(name = "SignatureHash", length = 256)
    private String signatureHash;

    @Builder.Default
    @Column(name = "ProcessStatus", nullable = false)
    private Integer processStatus = 0;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
"@

# 54. NxvBillingTransactions
New-NxvEntity "NxvBillingTransactions" "NxvBillingTransactions" "Long" @"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BillingTxID")
    private Long billingTxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", nullable = false)
    private NxvPaymentOrders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "AmountVND", nullable = false)
    private Integer amountVnd;

    @Builder.Default
    @Column(name = "ConfirmedAt", nullable = false)
    private LocalDateTime confirmedAt = LocalDateTime.now();
"@

# ==============================================================================
# 5. SINH 5 READ-ONLY ENTITIES CHO 5 DATABASE VIEWS
# ==============================================================================
New-NxvEntity "NxvVwUserTotalBuffs" "NxvVw_UserTotalBuffs" "Long" @"
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "TotalBuffMaxHP")
    private Integer totalBuffMaxHp;

    @Column(name = "TotalBuffMaxMP")
    private Integer totalBuffMaxMp;

    @Column(name = "TotalEXPBonus")
    private BigDecimal totalExpBonus;

    @Column(name = "TotalGoldBonus")
    private BigDecimal totalGoldBonus;

    @Column(name = "TotalDmgReduce")
    private BigDecimal totalDmgReduce;
"@ $true

New-NxvEntity "NxvVwLevelLeaderboard" "NxvVw_LevelLeaderboard" "Long" @"
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "CharacterName")
    private String characterName;

    @Column(name = "CurrentLevel")
    private Integer currentLevel;

    @Column(name = "CurrentEXP")
    private Long currentExp;

    @Column(name = "Gold")
    private Long gold;
"@ $true

New-NxvEntity "NxvVwGoldLeaderboard" "NxvVw_GoldLeaderboard" "Long" @"
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "CharacterName")
    private String characterName;

    @Column(name = "CurrentLevel")
    private Integer currentLevel;

    @Column(name = "Gold")
    private Long gold;
"@ $true

New-NxvEntity "NxvVwTaskLeaderboard" "NxvVw_TaskLeaderboard" "Long" @"
    @Id
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "CharacterName")
    private String characterName;

    @Column(name = "TotalTasksDone")
    private Integer totalTasksDone;
"@ $true

New-NxvEntity "NxvVwOverdueTasks" "NxvVw_OverdueTasks" "Long" @"
    @Id
    @Column(name = "TaskID")
    private Long taskId;

    @Column(name = "UserID")
    private Long userId;

    @Column(name = "PartyTaskID")
    private Long partyTaskId;

    @Column(name = "Title")
    private String title;

    @Column(name = "Difficulty")
    private Integer difficulty;

    @Column(name = "PenaltyHP")
    private Integer penaltyHp;

    @Column(name = "DeadlineAt")
    private LocalDateTime deadlineAt;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "IsShielded")
    private Boolean isShielded;
"@ $true

# ==============================================================================
# 6. REPOSITORIES TICH HOP SAN 7 STORED PROCEDURES CUA V2.2
# ==============================================================================
Write-JavaFile "$baseDir\nxvrepository\UsersRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
"@

Write-JavaFile "$baseDir\nxvrepository\NxvCharactersRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvCharacters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NxvCharactersRepository extends JpaRepository<NxvCharacters, Long> {
    Optional<NxvCharacters> findByUser_UserId(Long userId);
}
"@

# Tich hop NxvSp_CompleteTask va NxvSp_CronDailyDeadlineCheck
Write-JavaFile "$baseDir\nxvrepository\NxvTasksRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface NxvTasksRepository extends JpaRepository<NxvTasks, Long> {
    List<NxvTasks> findByUser_UserIdOrderBySortOrderAscCreatedAtDesc(Long userId);

    @Query(value = "EXEC NxvSp_CompleteTask @TaskID = :taskId, @UserID = :userId", nativeQuery = true)
    Map<String, Object> executeCompleteTaskSp(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_CronDailyDeadlineCheck", nativeQuery = true)
    void executeCronDailyDeadlineCheckSp();
}
"@

# Tich hop NxvSp_CalculateGPA
Write-JavaFile "$baseDir\nxvrepository\NxvEpicQuestsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvEpicQuests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NxvEpicQuestsRepository extends JpaRepository<NxvEpicQuests, Long> {
    List<NxvEpicQuests> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = "EXEC NxvSp_CalculateGPA @EpicQuestID = :epicQuestId", nativeQuery = true)
    BigDecimal executeCalculateGpaSp(@Param("epicQuestId") Long epicQuestId);
}
"@

# Tich hop NxvSp_EquipItem, NxvSp_UnequipItem, NxvSp_UseConsumable
Write-JavaFile "$baseDir\nxvrepository\NxvInventoryRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvInventoryRepository extends JpaRepository<NxvInventory, Long> {
    List<NxvInventory> findByUser_UserId(Long userId);
    Optional<NxvInventory> findByUser_UserIdAndItem_ItemId(Long userId, Integer itemId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_EquipItem @UserID = :userId, @ItemID = :itemId", nativeQuery = true)
    void executeEquipItemSp(@Param("userId") Long userId, @Param("itemId") Integer itemId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_UnequipItem @UserID = :userId, @SlotID = :slotId", nativeQuery = true)
    void executeUnequipItemSp(@Param("userId") Long userId, @Param("slotId") Integer slotId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_UseConsumable @UserID = :userId, @ItemID = :itemId", nativeQuery = true)
    void executeUseConsumableSp(@Param("userId") Long userId, @Param("itemId") Integer itemId);
}
"@

# Tich hop NxvSp_CompleteFocusSession
Write-JavaFile "$baseDir\nxvrepository\NxvFocusLogsRepository.java" @"
package $basePkg.nxvrepository;

import $basePkg.nxventity.NxvFocusLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NxvFocusLogsRepository extends JpaRepository<NxvFocusLogs, Long> {
    List<NxvFocusLogs> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query(value = "EXEC NxvSp_CompleteFocusSession @UserID = :userId, @DurationMinutes = :durationMinutes", nativeQuery = true)
    void executeCompleteFocusSessionSp(@Param("userId") Long userId, @Param("durationMinutes") Integer durationMinutes);
}
"@

# ==============================================================================
# 7. SINH PACKAGE nxvconfig & HEALTH CHECK CONTROLLER
# ==============================================================================
Write-JavaFile "$baseDir\nxvconfig\NxvSecurityConfig.java" @"
package $basePkg.nxvconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class NxvSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
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

Write-JavaFile "$baseDir\nxvcontroller\NxvSystemHealthController.java" @"
package $basePkg.nxvcontroller;

import $basePkg.nxvdto.ApiResponse;
import $basePkg.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class NxvSystemHealthController {

    private final UsersRepository usersRepository;
    private final NxvLevelConfigRepository levelConfigRepository;
    private final NxvClassesRepository classesRepository;
    private final NxvItemsRepository itemsRepository;
    private final NxvSkillDefinitionsRepository skillDefinitionsRepository;
    private final NxvPetSpeciesRepository petSpeciesRepository;
    private final NxvTitlesRepository titlesRepository;
    private final NxvAchievementsRepository achievementsRepository;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("status", "UP - Connected to NxvTaskTaleDB v2.2 (54 Tables, 5 Views, 7 SPs)");
        stats.put("totalUsers", usersRepository.count());
        stats.put("seedLevelsCount", levelConfigRepository.count());
        stats.put("seedClassesCount", classesRepository.count());
        stats.put("seedItemsCount", itemsRepository.count());
        stats.put("seedSkillsCount", skillDefinitionsRepository.count());
        stats.put("seedPetSpeciesCount", petSpeciesRepository.count());
        stats.put("seedTitlesCount", titlesRepository.count());
        stats.put("seedAchievementsCount", achievementsRepository.count());
        return ApiResponse.ok("Kiá»ƒm tra káº¿t ná»‘i 54 báº£ng NxvTaskTaleDB v2.2 thÃ nh cÃ´ng!", stats);
    }
}
"@

Write-Host "==> HOAN TAT SINH 125+ FILE TASK 0 (SCHEMA V2.2: 54 BANG, 5 VIEWS, 7 SPs) THANH CONG!" -ForegroundColor Green
