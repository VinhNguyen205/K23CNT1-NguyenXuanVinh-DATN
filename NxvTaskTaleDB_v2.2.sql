-- ============================================================
-- TASKTALE / LIFERPG -- SQL SERVER SCHEMA (MASTER VERSION 2.2)
-- Đã đồng bộ 100% với 122 chức năng Đồ án tốt nghiệp:
-- Level Up tự động, Skill Points, Khiên chắn, Pet Buff, Party Chat, Notifications
-- V2.1 PATCH NOTES (so với V2.0):
--   + Phục hồi 4 Stored Procedure bị rớt mất khi lên V2.0:
--       NxvSp_EquipItem, NxvSp_UseConsumable,
--       NxvSp_CronDailyDeadlineCheck, NxvSp_CompleteFocusSession
--   + Thêm bảng NxvTitles + NxvUserTitles (danh hiệu/title, gắn FK vào
--     NxvCharacters.CurrentTitleID vốn trước đó không có bảng đích)
--   + Thêm bảng NxvAchievements + NxvUserAchievements (thành tựu - tính
--     năng #101, và dùng làm cơ chế trao "danh hiệu độc quyền" khi hoàn
--     thành Epic Quest như mô tả trong đồ án)
-- V2.2 PATCH NOTES (so với V2.1, sau review của Gemini):
--   + Thêm cột NxvCharacters.BaseMaxHP / BaseMaxMP: tách riêng "máu/mana
--     GỐC theo Level" khỏi "máu/mana HIỆU LỰC" (MaxHP/MaxMP = Base + Buff
--     trang bị). Đây là root-fix cho bug #1 và #2 bên dưới - trước đó cả
--     2 proc cùng ghi đè lên 1 cột MaxHP/MaxMP nên luôn có 1 bên bị mất dữ liệu.
--   + [FIX BUG] NxvSp_EquipItem: Gemini đã tự vá hardcode 100/50 -> đọc
--     theo NxvLevelConfig, V2.2 tinh gọn lại thành đọc thẳng BaseMaxHP/
--     BaseMaxMP đã lưu trên nhân vật (tránh lệch khi Level > 10, ngoài bảng
--     NxvLevelConfig).
--   + [FIX BUG] NxvSp_CompleteTask (đoạn Level Up): trước đây lấy MaxHP/MaxMP
--     thuần từ NxvLevelConfig rồi ghi đè thẳng -> xoá mất buff trang bị đang
--     mặc. Giờ cập nhật BaseMaxHP/BaseMaxMP trước, sau đó cộng lại buff trang
--     bị hiện có để ra MaxHP/MaxMP hiệu lực.
--   + [FIX BUG] NxvSp_CronDailyDeadlineCheck: bổ sung kiểm tra
--     NxvCharacters.IsShieldActive (khiên cấp nhân vật, vd. từ skill/pet/
--     item khác với IsShielded cấp-task) - trước đây cột này tồn tại trong
--     schema nhưng không có proc nào đọc/ghi, tức là tính năng khiên chắn
--     nhân vật coi như vô hiệu trên toàn hệ thống.
--   + [BỔ SUNG] NxvSp_UnequipItem: V2.1 có Equip nhưng thiếu hẳn Unequip,
--     khiến người chơi không có đường quay lại trạng thái "tay không" cho
--     1 slot - bổ sung cho trọn vẹn cơ chế trang bị vừa sửa ở trên.
-- ============================================================

USE master;
GO

IF DB_ID('NxvTaskTaleDB') IS NOT NULL
BEGIN
    ALTER DATABASE NxvTaskTaleDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE NxvTaskTaleDB;
END
GO

CREATE DATABASE NxvTaskTaleDB COLLATE Vietnamese_CI_AS;
GO

USE NxvTaskTaleDB;
GO

-- ============================================================
-- 1. BASE TABLES & CONFIGS (Tạo trước để tránh lỗi FK)
-- ============================================================

CREATE TABLE Users (
    UserID              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    Username            NVARCHAR(50)    NOT NULL UNIQUE,
    Email               NVARCHAR(150)   NOT NULL UNIQUE,
    PasswordHash        NVARCHAR(256)   NULL,
    Gender              TINYINT         NOT NULL DEFAULT 0,  -- 0=Nam, 1=Nữ
    DefaultAvatarUrl    NVARCHAR(500)   NULL,
    Role                TINYINT         NOT NULL DEFAULT 0,  -- 0=User, 1=Moderator, 2=SuperAdmin
    IsEmailVerified     BIT             NOT NULL DEFAULT 0,
    IsLocked            BIT             NOT NULL DEFAULT 0,
    IsDead              BIT             NOT NULL DEFAULT 0,
    LoginFailCount      TINYINT         NOT NULL DEFAULT 0,
    LastLoginAt         DATETIME2       NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    DeletedAt           DATETIME2       NULL
);
GO

CREATE TABLE NxvLevelConfig (
    Level               INT             PRIMARY KEY,
    EXPRequired         BIGINT          NOT NULL,
    MaxHP               INT             NOT NULL DEFAULT 100,
    MaxMP               INT             NOT NULL DEFAULT 50
);
GO

CREATE TABLE NxvTags (
    TagID               INT             IDENTITY(1,1) PRIMARY KEY,
    TagName             NVARCHAR(50)    NOT NULL UNIQUE,
    ColorHex            NVARCHAR(7)     NOT NULL DEFAULT '#FFFFFF'
);
GO

CREATE TABLE NxvEquipmentSlots (
    SlotID              INT             IDENTITY(1,1) PRIMARY KEY,
    SlotName            NVARCHAR(50)    NOT NULL,
    SlotKey             NVARCHAR(30)    NOT NULL UNIQUE
);
GO

CREATE TABLE NxvClasses (
    ClassID             INT             IDENTITY(1,1) PRIMARY KEY,
    ClassName           NVARCHAR(100)   NOT NULL,
    PassiveEXPBonus     DECIMAL(5,4)    NOT NULL DEFAULT 0,
    PassiveGoldBonus    DECIMAL(5,4)    NOT NULL DEFAULT 0
);
GO

CREATE TABLE NxvPetSpecies (
    SpeciesID               INT             IDENTITY(1,1) PRIMARY KEY,
    SpeciesName             NVARCHAR(100)   NOT NULL,
    ElementType             NVARCHAR(50)    NOT NULL DEFAULT N'Tự nhiên',
    EggImageUrl             NVARCHAR(500)   NULL,
    BabyImageUrl            NVARCHAR(500)   NULL,
    AdultImageUrl           NVARCHAR(500)   NULL,
    EvolveDays              INT             NOT NULL DEFAULT 14,
    PassiveGoldBonus        DECIMAL(5,4)    NOT NULL DEFAULT 0,
    PassiveDropRateBonus    DECIMAL(5,4)    NOT NULL DEFAULT 0
);
GO

-- [V2.1] Danh hiệu (Title/Badge) - trao từ Achievement hoặc Epic Quest hiếm
CREATE TABLE NxvTitles (
    TitleID             INT             IDENTITY(1,1) PRIMARY KEY,
    TitleName           NVARCHAR(100)   NOT NULL,
    TitleIcon           NVARCHAR(500)   NULL,
    IsExclusive         BIT             NOT NULL DEFAULT 0   -- Danh hiệu độc quyền (từ Epic Quest hiếm)
);
GO

-- ============================================================
-- 2. MODULE TÀI KHOẢN & NHÂN VẬT
-- ============================================================

CREATE TABLE NxvOAuthProviders (
    OAuthID             BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    Provider            NVARCHAR(30)    NOT NULL,
    ProviderUserID      NVARCHAR(200)   NOT NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_OAuth UNIQUE (Provider, ProviderUserID)
);
GO

CREATE TABLE NxvOtpTokens (
    TokenID             BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    TokenType           TINYINT         NOT NULL, -- 0=VerifyEmail, 1=ResetPassword
    TokenHash           NVARCHAR(256)   NOT NULL,
    ExpiresAt           DATETIME2       NOT NULL,
    IsUsed              BIT             NOT NULL DEFAULT 0,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvUserSessions (
    SessionID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    DeviceName          NVARCHAR(200)   NULL,
    DeviceToken         NVARCHAR(500)   NOT NULL,
    IsActive            BIT             NOT NULL DEFAULT 1,
    LastActiveAt        DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvCharacters (
    CharacterID         BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL UNIQUE REFERENCES Users(UserID),
    CharacterName       NVARCHAR(100)   NOT NULL,
    AvatarUrl           NVARCHAR(500)   NULL,
    AvatarFrameUrl      NVARCHAR(500)   NULL,
    CurrentLevel        INT             NOT NULL DEFAULT 1,
    CurrentEXP          BIGINT          NOT NULL DEFAULT 0,
    EXPToNextLevel      BIGINT          NOT NULL DEFAULT 100,
    BaseMaxHP           INT             NOT NULL DEFAULT 100,   -- [V2.2] Máu gốc THEO LEVEL (không tính buff trang bị)
    BaseMaxMP           INT             NOT NULL DEFAULT 50,    -- [V2.2] Mana gốc THEO LEVEL (không tính buff trang bị)
    MaxHP               INT             NOT NULL DEFAULT 100,   -- Máu HIỆU LỰC = BaseMaxHP + tổng buff trang bị
    CurrentHP           INT             NOT NULL DEFAULT 100,
    MaxMP               INT             NOT NULL DEFAULT 50,    -- Mana HIỆU LỰC = BaseMaxMP + tổng buff trang bị
    CurrentMP           INT             NOT NULL DEFAULT 50,
    Gold                BIGINT          NOT NULL DEFAULT 0,
    SkillPoints         INT             NOT NULL DEFAULT 0,
    IsShieldActive      BIT             NOT NULL DEFAULT 0,
    TotalTasksDone      INT             NOT NULL DEFAULT 0,
    CurrentClassID      INT             NULL REFERENCES NxvClasses(ClassID),
    CurrentTitleID      INT             NULL REFERENCES NxvTitles(TitleID),   -- [V2.1] giờ đã có bảng đích
    StreakDays          INT             NOT NULL DEFAULT 0,
    LastCheckInDate     DATE            NULL,
    UpdatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CHK_NxvChar_HP CHECK (CurrentHP >= 0 AND CurrentHP <= MaxHP),
    CONSTRAINT CHK_NxvChar_MP CHECK (CurrentMP >= 0 AND CurrentMP <= MaxMP)
);
GO

-- [V2.1] Danh hiệu người chơi đã mở khóa (mặc định được Equip qua CurrentTitleID ở trên)
CREATE TABLE NxvUserTitles (
    UserTitleID         BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    TitleID             INT             NOT NULL REFERENCES NxvTitles(TitleID),
    UnlockedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_UserTitle UNIQUE (UserID, TitleID)
);
GO

CREATE TABLE NxvStatLogs (
    LogID               BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    LogType             NVARCHAR(30)    NOT NULL,   -- 'EXP', 'Gold', 'HP', 'MP', 'LevelUp'
    Delta               BIGINT          NOT NULL,
    SourceType          NVARCHAR(50)    NULL,
    SourceID            BIGINT          NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

-- ============================================================
-- 3. MODULE XÃ HỘI & TỔ ĐỘI
-- ============================================================

CREATE TABLE NxvParties (
    PartyID             BIGINT          IDENTITY(1,1) PRIMARY KEY,
    PartyName           NVARCHAR(100)   NOT NULL,
    Description         NVARCHAR(300)   NULL,
    LeaderUserID        BIGINT          NOT NULL REFERENCES Users(UserID),
    MaxMembers          INT             NOT NULL DEFAULT 5,
    IsActive            BIT             NOT NULL DEFAULT 1,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvPartyMembers (
    PartyMemberID       BIGINT          IDENTITY(1,1) PRIMARY KEY,
    PartyID             BIGINT          NOT NULL REFERENCES NxvParties(PartyID),
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    Role                TINYINT         NOT NULL DEFAULT 0, -- 0=Member, 1=Leader, 2=PendingInvite
    JoinedAt            DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_PartyMember UNIQUE (PartyID, UserID)
);
GO

CREATE TABLE NxvPartyMessages (
    MessageID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    PartyID             BIGINT          NOT NULL REFERENCES NxvParties(PartyID),
    SenderUserID        BIGINT          NOT NULL REFERENCES Users(UserID),
    Content             NVARCHAR(1000)  NOT NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvFriendships (
    FriendshipID        BIGINT          IDENTITY(1,1) PRIMARY KEY,
    RequesterID         BIGINT          NOT NULL REFERENCES Users(UserID),
    AddresseeID         BIGINT          NOT NULL REFERENCES Users(UserID),
    Status              TINYINT         NOT NULL DEFAULT 0, -- 0=Pending, 1=Accepted, 2=Declined
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_Friendship UNIQUE (RequesterID, AddresseeID)
);
GO

-- ============================================================
-- 4. MODULE QUẢN LÝ NHIỆM VỤ
-- ============================================================

CREATE TABLE NxvEpicQuests (
    EpicQuestID         BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    PartyID             BIGINT          NULL REFERENCES NxvParties(PartyID),
    Title               NVARCHAR(200)   NOT NULL,
    IsGPAQuest          BIT             NOT NULL DEFAULT 0,
    TargetGPA           DECIMAL(4,2)    NULL,
    RewardEXP           INT             NOT NULL DEFAULT 0,
    RewardGold          INT             NOT NULL DEFAULT 0,
    Status              TINYINT         NOT NULL DEFAULT 0, -- 0=InProgress, 1=Completed, 2=Cancelled
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvTasks (
    TaskID              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    EpicQuestID         BIGINT          NULL REFERENCES NxvEpicQuests(EpicQuestID),
    PartyTaskID         BIGINT          NULL REFERENCES NxvTasks(TaskID),
    Title               NVARCHAR(200)   NOT NULL,
    DescriptionMarkdown NVARCHAR(MAX)   NULL,
    ColorLabel          NVARCHAR(7)     NULL,
    Difficulty          TINYINT         NOT NULL DEFAULT 1, -- 1=Easy, 2=Normal, 3=Hard
    RewardEXP           INT             NOT NULL DEFAULT 0,
    RewardGold          INT             NOT NULL DEFAULT 0,
    RewardMP            INT             NOT NULL DEFAULT 0,
    PenaltyHP           INT             NOT NULL DEFAULT 0,
    Status              TINYINT         NOT NULL DEFAULT 0, -- 0=Pending, 1=Completed, 2=Failed, 3=Skipped
    SortOrder           INT             NOT NULL DEFAULT 0, -- Phục vụ kéo thả Kanban
    DeadlineAt          DATETIME2       NULL,
    ReminderBeforeMins  INT             NULL,
    IsFrozen            BIT             NOT NULL DEFAULT 0,
    IsShielded          BIT             NOT NULL DEFAULT 0, -- Khiên chắn bảo vệ không trừ HP khi trễ hạn
    UndoAvailable       BIT             NOT NULL DEFAULT 0,
    CompletedAt         DATETIME2       NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvTaskTemplates (
    TemplateID          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    Title               NVARCHAR(200)   NOT NULL,
    Difficulty          TINYINT         NOT NULL DEFAULT 1,
    RecurringRule       NVARCHAR(100)   NOT NULL,
    IsActive            BIT             NOT NULL DEFAULT 1
);
GO

CREATE TABLE NxvSubTasks (
    SubTaskID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    EpicQuestID         BIGINT          NOT NULL REFERENCES NxvEpicQuests(EpicQuestID),
    Title               NVARCHAR(200)   NOT NULL,
    IsDone              BIT             NOT NULL DEFAULT 0,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvTaskTags (
    TaskTagID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    TaskID              BIGINT          NOT NULL REFERENCES NxvTasks(TaskID),
    TagID               INT             NOT NULL REFERENCES NxvTags(TagID),
    CONSTRAINT UQ_Nxv_TaskTag UNIQUE (TaskID, TagID)
);
GO

CREATE TABLE NxvGPASubjects (
    SubjectID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    EpicQuestID         BIGINT          NOT NULL REFERENCES NxvEpicQuests(EpicQuestID),
    SubjectName         NVARCHAR(200)   NOT NULL,
    Credits             TINYINT         NOT NULL DEFAULT 3,
    Score               DECIMAL(4,2)    NULL,
    IsExcluded          BIT             NOT NULL DEFAULT 0,
    ExcludeReason       NVARCHAR(100)   NULL
);
GO

CREATE TABLE NxvTaskHistory (
    HistoryID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    TaskID              BIGINT          NOT NULL REFERENCES NxvTasks(TaskID),
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    ActionType          NVARCHAR(20)    NOT NULL,
    ActionAt            DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

-- ============================================================
-- 5. MODULE CÂY KỸ NĂNG & CLASS
-- ============================================================

CREATE TABLE NxvUserUnlockedClasses (
    UnlockID            BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    ClassID             INT             NOT NULL REFERENCES NxvClasses(ClassID),
    UnlockedAt          DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_UserClass UNIQUE (UserID, ClassID)
);
GO

CREATE TABLE NxvClassChangeLogs (
    LogID               BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    FromClassID         INT             NULL REFERENCES NxvClasses(ClassID),
    ToClassID           INT             NOT NULL REFERENCES NxvClasses(ClassID),
    ChangedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvSkillDefinitions (
    SkillID             INT             IDENTITY(1,1) PRIMARY KEY,
    SkillName           NVARCHAR(100)   NOT NULL,
    SkillType           TINYINT         NOT NULL DEFAULT 0, -- 0=Active, 1=Passive
    MPCost              INT             NOT NULL DEFAULT 0,
    CooldownSeconds     INT             NOT NULL DEFAULT 0,
    RequiredLevel       INT             NOT NULL DEFAULT 1,
    MaxSkillLevel       INT             NOT NULL DEFAULT 5,
    ParentSkillID       INT             NULL REFERENCES NxvSkillDefinitions(SkillID),
    SkillEffect         NVARCHAR(50)    NULL  -- 'DASH_24H', 'SHIELD_TASK', ...
);
GO

CREATE TABLE NxvUserSkills (
    UserSkillID         BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    SkillID             INT             NOT NULL REFERENCES NxvSkillDefinitions(SkillID),
    IsUnlocked          BIT             NOT NULL DEFAULT 0,
    SkillLevel          INT             NOT NULL DEFAULT 1,
    CooldownEndsAt      DATETIME2       NULL,
    CONSTRAINT UQ_Nxv_UserSkill UNIQUE (UserID, SkillID)
);
GO

CREATE TABLE NxvSkillUsageLogs (
    UsageID             BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    SkillID             INT             NOT NULL REFERENCES NxvSkillDefinitions(SkillID),
    TargetTaskID        BIGINT          NULL REFERENCES NxvTasks(TaskID),
    UsedAt              DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvUserTagMastery (
    MasteryID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    TagID               INT             NOT NULL REFERENCES NxvTags(TagID),
    TaskCount           INT             NOT NULL DEFAULT 0,
    UpdatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_UserTagMastery UNIQUE (UserID, TagID)
);
GO

-- ============================================================
-- 6. MODULE THÚ CƯNG ĐỒNG HÀNH (PETS)
-- ============================================================

CREATE TABLE NxvUserPets (
    UserPetID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    SpeciesID           INT             NOT NULL REFERENCES NxvPetSpecies(SpeciesID),
    PetName             NVARCHAR(100)   NOT NULL DEFAULT N'Thú Cưng',
    ElementType         NVARCHAR(50)    NULL,
    Stage               TINYINT         NOT NULL DEFAULT 0, -- 0=Trứng, 1=Baby, 2=Trưởng thành
    CurrentImageUrl     NVARCHAR(500)   NULL,
    Hunger              INT             NOT NULL DEFAULT 100 CHECK (Hunger >= 0 AND Hunger <= 100),
    Affection           INT             NOT NULL DEFAULT 50 CHECK (Affection >= 0 AND Affection <= 100),
    IsActive            BIT             NOT NULL DEFAULT 0,
    IsHibernating       BIT             NOT NULL DEFAULT 0,
    HatchedAt           DATETIME2       NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvPetInteractionLogs (
    LogID               BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserPetID           BIGINT          NOT NULL REFERENCES NxvUserPets(UserPetID),
    InteractionType     NVARCHAR(30)    NOT NULL, -- 'FEED', 'PET', 'AI_REMIND', 'AI_PRAISE'
    UsedItemID          INT             NULL,
    AIMessage           NVARCHAR(1000)  NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvPetDex (
    PetDexID            BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    SpeciesID           INT             NOT NULL REFERENCES NxvPetSpecies(SpeciesID),
    FirstObtainedAt     DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_PetDex UNIQUE (UserID, SpeciesID)
);
GO

-- ============================================================
-- 7. MODULE GACHA & INVENTORY
-- ============================================================

CREATE TABLE NxvItems (
    ItemID                  INT             IDENTITY(1,1) PRIMARY KEY,
    ItemName                NVARCHAR(200)   NOT NULL,
    ItemType                TINYINT         NOT NULL, -- 0=Equipment, 1=Consumable, 2=Avatar, 3=AvatarFrame, 4=PetEgg
    ImageUrl                NVARCHAR(500)   NULL,
    EquipSlotID             INT             NULL REFERENCES NxvEquipmentSlots(SlotID),
    BuffMaxHP               INT             NOT NULL DEFAULT 0,
    BuffMaxMP               INT             NOT NULL DEFAULT 0,
    BuffEXPBonus            DECIMAL(5,4)    NOT NULL DEFAULT 0,
    BuffGoldBonus           DECIMAL(5,4)    NOT NULL DEFAULT 0,
    BuffDeadlineDmgReduce   DECIMAL(5,4)    NOT NULL DEFAULT 0,
    HealHP                  INT             NOT NULL DEFAULT 0,
    SellPrice               INT             NOT NULL DEFAULT 0
);
GO

CREATE TABLE NxvUserInventoryConfig (
    UserID              BIGINT          PRIMARY KEY REFERENCES Users(UserID),
    MaxSlots            INT             NOT NULL DEFAULT 50
);
GO

CREATE TABLE NxvInventory (
    InventoryID         BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    ItemID              INT             NOT NULL REFERENCES NxvItems(ItemID),
    Quantity            INT             NOT NULL DEFAULT 1 CHECK (Quantity >= 0),
    CONSTRAINT UQ_Nxv_Inventory UNIQUE (UserID, ItemID)
);
GO

CREATE TABLE NxvEquippedItems (
    EquippedID          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    SlotID              INT             NOT NULL REFERENCES NxvEquipmentSlots(SlotID),
    ItemID              INT             NOT NULL REFERENCES NxvItems(ItemID),
    CONSTRAINT UQ_Nxv_EquipSlot UNIQUE (UserID, SlotID)
);
GO

CREATE TABLE NxvGachaBanners (
    BannerID            INT             IDENTITY(1,1) PRIMARY KEY,
    BannerName          NVARCHAR(150)   NOT NULL DEFAULT N'Hộp Mù Bí Ẩn',
    BannerImageUrl      NVARCHAR(500)   NULL,
    CostPerPull         INT             NOT NULL DEFAULT 100,
    PityLimit           INT             NOT NULL DEFAULT 50,
    IsActive            BIT             NOT NULL DEFAULT 1
);
GO

CREATE TABLE NxvGachaDropRates (
    DropRateID          INT             IDENTITY(1,1) PRIMARY KEY,
    BannerID            INT             NOT NULL REFERENCES NxvGachaBanners(BannerID),
    ItemID              INT             NOT NULL REFERENCES NxvItems(ItemID),
    DropRate            DECIMAL(8,6)    NOT NULL,
    IsFeatured          BIT             NOT NULL DEFAULT 0
);
GO

CREATE TABLE NxvUserPityCounters (
    PityID              BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    BannerID            INT             NOT NULL REFERENCES NxvGachaBanners(BannerID),
    PullCount           INT             NOT NULL DEFAULT 0,
    TotalPulls          INT             NOT NULL DEFAULT 0,
    CONSTRAINT UQ_Nxv_Pity UNIQUE (UserID, BannerID)
);
GO

CREATE TABLE NxvGachaHistory (
    GachaHistoryID      BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    BannerID            INT             NOT NULL REFERENCES NxvGachaBanners(BannerID),
    ItemID              INT             NOT NULL REFERENCES NxvItems(ItemID),
    IsPity              BIT             NOT NULL DEFAULT 0,
    GoldSpent           INT             NOT NULL DEFAULT 0,
    PulledAt            DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvMarketListings (
    ListingID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    SellerUserID        BIGINT          NOT NULL REFERENCES Users(UserID),
    ItemID              INT             NOT NULL REFERENCES NxvItems(ItemID),
    Quantity            INT             NOT NULL DEFAULT 1,
    PricePerUnit        INT             NOT NULL,
    Status              TINYINT         NOT NULL DEFAULT 0, -- 0=Active, 1=Sold, 2=Cancelled
    TaxRate             DECIMAL(5,4)    NOT NULL DEFAULT 0.05,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    SoldAt              DATETIME2       NULL
);
GO

CREATE TABLE NxvMarketTransactions (
    MarketTxID          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    ListingID           BIGINT          NOT NULL REFERENCES NxvMarketListings(ListingID),
    BuyerUserID         BIGINT          NOT NULL REFERENCES Users(UserID),
    TotalPrice          BIGINT          NOT NULL,
    TxAt                DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

-- [V2.1] Thành tựu (Achievement) - tính năng #101, có thể trao Title/Item/Gold/EXP
CREATE TABLE NxvAchievements (
    AchievementID       INT             IDENTITY(1,1) PRIMARY KEY,
    Code                NVARCHAR(50)    NOT NULL UNIQUE,
    AchievementName     NVARCHAR(150)   NOT NULL,
    Description         NVARCHAR(500)   NULL,
    IconUrl              NVARCHAR(500)   NULL,
    RewardTitleID       INT             NULL REFERENCES NxvTitles(TitleID),
    RewardItemID        INT             NULL REFERENCES NxvItems(ItemID),
    RewardGold          INT             NOT NULL DEFAULT 0,
    RewardEXP           INT             NOT NULL DEFAULT 0
);
GO

CREATE TABLE NxvUserAchievements (
    UserAchievementID   BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID               BIGINT          NOT NULL REFERENCES Users(UserID),
    AchievementID        INT             NOT NULL REFERENCES NxvAchievements(AchievementID),
    UnlockedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Nxv_UserAchievement UNIQUE (UserID, AchievementID)
);
GO

-- ============================================================
-- 8. MODULE IRL, THÔNG BÁO & ADMIN
-- ============================================================

CREATE TABLE NxvNotifications (
    NotificationID      BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NULL REFERENCES Users(UserID), -- NULL = Global Notification
    Title               NVARCHAR(200)   NOT NULL,
    Content             NVARCHAR(1000)  NOT NULL,
    NotificationType    NVARCHAR(30)    NOT NULL DEFAULT 'SYSTEM', -- 'REMINDER', 'GLOBAL', 'PARTY', 'SYSTEM'
    IsRead              BIT             NOT NULL DEFAULT 0,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvJournals (
    JournalID           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    Content             NVARCHAR(MAX)   NOT NULL,
    MoodTag             NVARCHAR(50)    NULL,
    AIResponse          NVARCHAR(1000)  NULL,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvFocusLogs (
    FocusLogID          BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    DurationMinutes     INT             NOT NULL,
    RewardEXP           INT             NOT NULL DEFAULT 0,
    RewardGold          INT             NOT NULL DEFAULT 0,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvUserReports (
    ReportID            BIGINT          IDENTITY(1,1) PRIMARY KEY,
    ReporterUserID      BIGINT          NOT NULL REFERENCES Users(UserID),
    TargetUserID        BIGINT          NULL REFERENCES Users(UserID),
    ReportType          NVARCHAR(50)    NOT NULL,
    Description         NVARCHAR(2000)  NOT NULL,
    Status              TINYINT         NOT NULL DEFAULT 0, -- 0=Pending, 1=Resolved, 2=Rejected
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvAIConfig (
    ConfigID            INT             IDENTITY(1,1) PRIMARY KEY,
    Provider            NVARCHAR(30)    NOT NULL,
    ModelName           NVARCHAR(100)   NOT NULL,
    APIKeyEncrypted     NVARCHAR(1000)  NOT NULL,
    SystemPrompt        NVARCHAR(MAX)   NULL,
    IsActive            BIT             NOT NULL DEFAULT 1,
    UpdatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

-- ============================================================
-- 9. MODULE PAYMENT & SINH TỬ
-- ============================================================

CREATE TABLE NxvRevivalPackages (
    PackageID           INT             IDENTITY(1,1) PRIMARY KEY,
    PackageName         NVARCHAR(100)   NOT NULL,
    AmountVND           INT             NOT NULL
);
GO

CREATE TABLE NxvPaymentOrders (
    OrderID             BIGINT          IDENTITY(1,1) PRIMARY KEY,
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    OrderCode           NVARCHAR(50)    NOT NULL UNIQUE,
    AmountVND           INT             NOT NULL,
    Status              TINYINT         NOT NULL DEFAULT 0, -- 0=Pending, 1=Paid, 2=Cancelled
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvWebhookLogs (
    WebhookLogID        BIGINT          IDENTITY(1,1) PRIMARY KEY,
    OrderCode           NVARCHAR(50)    NOT NULL,
    PayloadJson         NVARCHAR(MAX)   NULL,
    SignatureHash       NVARCHAR(256)   NULL,
    ProcessStatus       TINYINT         NOT NULL DEFAULT 0,
    CreatedAt           DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE NxvBillingTransactions (
    BillingTxID         BIGINT          IDENTITY(1,1) PRIMARY KEY,
    OrderID             BIGINT          NOT NULL REFERENCES NxvPaymentOrders(OrderID),
    UserID              BIGINT          NOT NULL REFERENCES Users(UserID),
    AmountVND           INT             NOT NULL,
    ConfirmedAt         DATETIME2       NOT NULL DEFAULT SYSDATETIME()
);
GO

-- ============================================================
-- VIEWS
-- ============================================================

CREATE OR ALTER VIEW NxvVw_UserTotalBuffs AS
SELECT
    ei.UserID,
    SUM(i.BuffMaxHP)                AS TotalBuffMaxHP,
    SUM(i.BuffMaxMP)                AS TotalBuffMaxMP,
    SUM(i.BuffEXPBonus)             AS TotalEXPBonus,
    SUM(i.BuffGoldBonus)            AS TotalGoldBonus,
    SUM(i.BuffDeadlineDmgReduce)    AS TotalDmgReduce
FROM NxvEquippedItems ei
JOIN NxvItems i ON ei.ItemID = i.ItemID
GROUP BY ei.UserID;
GO

CREATE OR ALTER VIEW NxvVw_LevelLeaderboard AS
SELECT TOP 100 UserID, CharacterName, CurrentLevel, CurrentEXP, Gold
FROM NxvCharacters ORDER BY CurrentLevel DESC, CurrentEXP DESC;
GO

CREATE OR ALTER VIEW NxvVw_GoldLeaderboard AS
SELECT TOP 100 UserID, CharacterName, CurrentLevel, Gold
FROM NxvCharacters ORDER BY Gold DESC;
GO

CREATE OR ALTER VIEW NxvVw_TaskLeaderboard AS
SELECT TOP 100 UserID, CharacterName, TotalTasksDone
FROM NxvCharacters ORDER BY TotalTasksDone DESC;
GO

CREATE OR ALTER VIEW NxvVw_OverdueTasks AS
SELECT TaskID, UserID, PartyTaskID, Title, Difficulty, PenaltyHP, DeadlineAt, Status, IsShielded
FROM NxvTasks
WHERE Status = 0 AND IsFrozen = 0 AND IsShielded = 0 AND DeadlineAt < SYSDATETIME();
GO

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

CREATE OR ALTER PROCEDURE NxvSp_CalculateGPA
    @EpicQuestID BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE NxvGPASubjects
    SET IsExcluded = 1, ExcludeReason = N'Điểm F'
    WHERE EpicQuestID = @EpicQuestID AND Score < 4.0 AND IsExcluded = 0;

    UPDATE NxvGPASubjects
    SET IsExcluded = 1, ExcludeReason = N'Môn GDQP - Không tính GPA'
    WHERE EpicQuestID = @EpicQuestID
      AND (SubjectName LIKE N'%GDQP%' OR SubjectName LIKE N'%Giáo dục quốc phòng%')
      AND IsExcluded = 0;

    WITH GradeCalc AS (
        SELECT Credits,
            CASE
                WHEN Score >= 8.6 THEN 4.0
                WHEN Score >= 8.0 THEN 3.5
                WHEN Score >= 7.0 THEN 3.0
                WHEN Score >= 6.5 THEN 2.5
                WHEN Score >= 5.5 THEN 2.0
                WHEN Score >= 5.0 THEN 1.5
                WHEN Score >= 4.0 THEN 1.0
                ELSE 0.0
            END AS GradePoint
        FROM NxvGPASubjects
        WHERE EpicQuestID = @EpicQuestID AND IsExcluded = 0
    )
    SELECT ROUND(CAST(SUM(Credits * GradePoint) AS DECIMAL(10,4)) / NULLIF(SUM(Credits), 0), 2) AS GPA
    FROM GradeCalc;
END;
GO

CREATE OR ALTER PROCEDURE NxvSp_CompleteTask
    @TaskID BIGINT, @UserID BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @RewardEXP INT, @RewardGold INT, @RewardMP INT, @Status TINYINT;
        SELECT @RewardEXP = RewardEXP, @RewardGold = RewardGold, @RewardMP = RewardMP, @Status = Status
        FROM NxvTasks WHERE TaskID = @TaskID AND UserID = @UserID;

        IF @@ROWCOUNT = 0 THROW 50010, N'Task không tồn tại.', 1;
        IF @Status <> 0   THROW 50011, N'Task đã xử lý rồi.', 1;
        IF EXISTS (SELECT 1 FROM Users WHERE UserID = @UserID AND IsDead = 1)
            THROW 50012, N'Nhân vật đang chết, không thể làm việc.', 1;

        -- 1. Tính Buff từ Trang bị
        DECLARE @EquipEXPBonus DECIMAL(5,4) = 0, @EquipGoldBonus DECIMAL(5,4) = 0;
        SELECT @EquipEXPBonus = ISNULL(TotalEXPBonus, 0), @EquipGoldBonus = ISNULL(TotalGoldBonus, 0)
        FROM NxvVw_UserTotalBuffs WHERE UserID = @UserID;

        -- 2. Tính Buff từ Class
        DECLARE @ClassEXPBonus DECIMAL(5,4) = 0, @ClassGoldBonus DECIMAL(5,4) = 0;
        SELECT @ClassEXPBonus = cl.PassiveEXPBonus, @ClassGoldBonus = cl.PassiveGoldBonus
        FROM NxvCharacters c JOIN NxvClasses cl ON c.CurrentClassID = cl.ClassID WHERE c.UserID = @UserID;

        -- 3. Tính Buff từ Pet đang đồng hành (Chỉ tính khi Pet đang Active, không ngủ đông và đã nở)
        DECLARE @PetGoldBonus DECIMAL(5,4) = 0;
        SELECT TOP 1 @PetGoldBonus = ps.PassiveGoldBonus
        FROM NxvUserPets up
        JOIN NxvPetSpecies ps ON up.SpeciesID = ps.SpeciesID
        WHERE up.UserID = @UserID AND up.IsActive = 1 AND up.IsHibernating = 0 AND up.Stage >= 1;

        DECLARE @FinalEXP INT = @RewardEXP + FLOOR(@RewardEXP * (@EquipEXPBonus + @ClassEXPBonus));
        DECLARE @FinalGold INT = @RewardGold + FLOOR(@RewardGold * (@EquipGoldBonus + @ClassGoldBonus + @PetGoldBonus));

        -- 4. Cập nhật trạng thái Task
        UPDATE NxvTasks
        SET Status = 1, UndoAvailable = 1, CompletedAt = SYSDATETIME(), UpdatedAt = SYSDATETIME()
        WHERE TaskID = @TaskID;

        INSERT INTO NxvTaskHistory (TaskID, UserID, ActionType)
        VALUES (@TaskID, @UserID, 'COMPLETE');

        -- 5. Cộng EXP, Gold, MP cho Nhân vật
        UPDATE NxvCharacters
        SET CurrentEXP = CurrentEXP + @FinalEXP,
            Gold = Gold + @FinalGold,
            CurrentMP = CASE WHEN CurrentMP + @RewardMP > MaxMP THEN MaxMP ELSE CurrentMP + @RewardMP END,
            TotalTasksDone = TotalTasksDone + 1,
            UpdatedAt = SYSDATETIME()
        WHERE UserID = @UserID;

        INSERT INTO NxvStatLogs (UserID, LogType, Delta, SourceType, SourceID)
        VALUES (@UserID, 'EXP', @FinalEXP, 'TaskComplete', @TaskID),
               (@UserID, 'Gold', @FinalGold, 'TaskComplete', @TaskID);

        -- 6. Tự động kiểm tra và xử lý Level Up + Cộng SkillPoints
        DECLARE @CurLevel INT, @CurEXP BIGINT, @NextEXP BIGINT, @LevelsGained INT = 0;
        SELECT @CurLevel = CurrentLevel, @CurEXP = CurrentEXP, @NextEXP = EXPToNextLevel
        FROM NxvCharacters WHERE UserID = @UserID;

        WHILE @CurEXP >= @NextEXP
        BEGIN
            SET @CurEXP = @CurEXP - @NextEXP;
            SET @CurLevel = @CurLevel + 1;
            SET @LevelsGained = @LevelsGained + 1;

            DECLARE @NewBaseMaxHP INT = NULL, @NewBaseMaxMP INT = NULL, @NewNextEXP BIGINT = NULL;
            SELECT @NewNextEXP = EXPRequired, @NewBaseMaxHP = MaxHP, @NewBaseMaxMP = MaxMP
            FROM NxvLevelConfig WHERE Level = @CurLevel;

            -- Công thức lũy tiến dự phòng nếu vượt quá bảng NxvLevelConfig
            IF @NewNextEXP IS NULL
            BEGIN
                SET @NewNextEXP = FLOOR(@NextEXP * 1.25);
                SELECT @NewBaseMaxHP = BaseMaxHP + 20, @NewBaseMaxMP = BaseMaxMP + 10 FROM NxvCharacters WHERE UserID = @UserID;
            END

            -- [V2.2] Lấy lại tổng buff trang bị đang mặc để cộng vào Base mới,
            -- tránh ghi đè mất buff khi lên cấp (bug Gemini phát hiện).
            DECLARE @EquipAddHP INT = 0, @EquipAddMP INT = 0;
            SELECT @EquipAddHP = ISNULL(TotalBuffMaxHP, 0), @EquipAddMP = ISNULL(TotalBuffMaxMP, 0)
            FROM NxvVw_UserTotalBuffs WHERE UserID = @UserID;

            UPDATE NxvCharacters
            SET CurrentLevel = @CurLevel,
                CurrentEXP = @CurEXP,
                EXPToNextLevel = @NewNextEXP,
                BaseMaxHP = @NewBaseMaxHP,
                BaseMaxMP = @NewBaseMaxMP,
                MaxHP = @NewBaseMaxHP + @EquipAddHP,
                CurrentHP = @NewBaseMaxHP + @EquipAddHP, -- Hồi đầy máu khi lên cấp
                MaxMP = @NewBaseMaxMP + @EquipAddMP,
                CurrentMP = @NewBaseMaxMP + @EquipAddMP, -- Hồi đầy mana khi lên cấp
                SkillPoints = SkillPoints + 1
            WHERE UserID = @UserID;

            SET @NextEXP = @NewNextEXP;
        END

        IF @LevelsGained > 0
        BEGIN
            INSERT INTO NxvStatLogs (UserID, LogType, Delta, SourceType, SourceID)
            VALUES (@UserID, 'LevelUp', @LevelsGained, 'TaskComplete', @TaskID);
        END

        COMMIT TRANSACTION;
        SELECT 'OK' AS Result, @FinalEXP AS EXPGained, @FinalGold AS GoldGained, @LevelsGained AS LevelsGained;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- [V2.1 - PHỤC HỒI] Mặc trang bị, tính lại MaxHP/MaxMP theo tổng Buff hiện có
CREATE OR ALTER PROCEDURE NxvSp_EquipItem
    @UserID BIGINT, @ItemID INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @SlotID INT, @ItemType TINYINT;
    SELECT @SlotID = EquipSlotID, @ItemType = ItemType FROM NxvItems WHERE ItemID = @ItemID;

    IF @ItemType <> 0 THROW 50100, N'Vật phẩm không phải trang bị', 1;
    IF NOT EXISTS (SELECT 1 FROM NxvInventory WHERE UserID = @UserID AND ItemID = @ItemID AND Quantity > 0)
        THROW 50101, N'Không sở hữu vật phẩm này', 1;

    BEGIN TRY
        BEGIN TRANSACTION;
            MERGE NxvEquippedItems AS tgt
            USING (SELECT @UserID AS UID, @SlotID AS SID, @ItemID AS IID) AS src
            ON tgt.UserID = src.UID AND tgt.SlotID = src.SID
            WHEN MATCHED THEN UPDATE SET ItemID = src.IID
            WHEN NOT MATCHED THEN INSERT (UserID, SlotID, ItemID) VALUES (src.UID, src.SID, src.IID);

            -- [V2.2] BaseMaxHP/BaseMaxMP là máu/mana gốc theo Level, đã được
            -- NxvSp_CompleteTask duy trì đúng mỗi lần lên cấp -> đọc thẳng,
            -- không suy ngược từ NxvLevelConfig nữa (tránh lệch khi Level > 10,
            -- ngoài phạm vi bảng NxvLevelConfig).
            DECLARE @BaseHP INT, @BaseMP INT;
            SELECT @BaseHP = BaseMaxHP, @BaseMP = BaseMaxMP FROM NxvCharacters WHERE UserID = @UserID;

            -- Cộng thêm tổng Buff từ toàn bộ trang bị đang mặc
            DECLARE @AddHP INT = 0, @AddMP INT = 0;
            SELECT @AddHP = ISNULL(TotalBuffMaxHP, 0), @AddMP = ISNULL(TotalBuffMaxMP, 0)
            FROM NxvVw_UserTotalBuffs WHERE UserID = @UserID;

            UPDATE NxvCharacters
            SET MaxHP = @BaseHP + @AddHP,
                MaxMP = @BaseMP + @AddMP,
                CurrentHP = CASE WHEN CurrentHP > @BaseHP + @AddHP THEN @BaseHP + @AddHP ELSE CurrentHP END,
                CurrentMP = CASE WHEN CurrentMP > @BaseMP + @AddMP THEN @BaseMP + @AddMP ELSE CurrentMP END,
                UpdatedAt = SYSDATETIME()
            WHERE UserID = @UserID;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- [V2.2 - BỔ SUNG] Tháo trang bị khỏi 1 Slot, trả MaxHP/MaxMP về đúng Base + buff còn lại
CREATE OR ALTER PROCEDURE NxvSp_UnequipItem
    @UserID BIGINT, @SlotID INT
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM NxvEquippedItems WHERE UserID = @UserID AND SlotID = @SlotID)
        THROW 50104, N'Slot này hiện không có trang bị nào', 1;

    BEGIN TRY
        BEGIN TRANSACTION;
            DELETE FROM NxvEquippedItems WHERE UserID = @UserID AND SlotID = @SlotID;

            DECLARE @BaseHP INT, @BaseMP INT, @AddHP INT = 0, @AddMP INT = 0;
            SELECT @BaseHP = BaseMaxHP, @BaseMP = BaseMaxMP FROM NxvCharacters WHERE UserID = @UserID;
            SELECT @AddHP = ISNULL(TotalBuffMaxHP, 0), @AddMP = ISNULL(TotalBuffMaxMP, 0)
            FROM NxvVw_UserTotalBuffs WHERE UserID = @UserID; -- đã tính trên trang bị CÒN LẠI sau khi DELETE

            UPDATE NxvCharacters
            SET MaxHP = @BaseHP + @AddHP,
                MaxMP = @BaseMP + @AddMP,
                CurrentHP = CASE WHEN CurrentHP > @BaseHP + @AddHP THEN @BaseHP + @AddHP ELSE CurrentHP END,
                CurrentMP = CASE WHEN CurrentMP > @BaseMP + @AddMP THEN @BaseMP + @AddMP ELSE CurrentMP END,
                UpdatedAt = SYSDATETIME()
            WHERE UserID = @UserID;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- [V2.1 - PHỤC HỒI] Dùng vật phẩm tiêu hao (VD: uống bình máu hồi HP)
CREATE OR ALTER PROCEDURE NxvSp_UseConsumable
    @UserID BIGINT, @ItemID INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @HealHP INT, @ItemType TINYINT;
    SELECT @HealHP = HealHP, @ItemType = ItemType FROM NxvItems WHERE ItemID = @ItemID;

    IF @ItemType <> 1 THROW 50102, N'Không phải vật phẩm tiêu hao', 1;

    BEGIN TRY
        BEGIN TRANSACTION;
            UPDATE NxvInventory SET Quantity = Quantity - 1 WHERE UserID = @UserID AND ItemID = @ItemID AND Quantity > 0;
            IF @@ROWCOUNT = 0 THROW 50103, N'Không đủ vật phẩm trong túi đồ', 1;

            UPDATE NxvCharacters
            SET CurrentHP = CASE WHEN CurrentHP + @HealHP > MaxHP THEN MaxHP ELSE CurrentHP + @HealHP END,
                UpdatedAt = SYSDATETIME()
            WHERE UserID = @UserID;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- [V2.1 - PHỤC HỒI + CẬP NHẬT] Cronjob quét task quá hạn mỗi đêm
-- So với V1: dùng NxvVw_OverdueTasks mới (đã tự lọc IsShielded = 0 ngay trong View),
-- ghi thêm NxvTaskHistory để đồng bộ audit trail với NxvSp_CompleteTask.
CREATE OR ALTER PROCEDURE NxvSp_CronDailyDeadlineCheck
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        DECLARE @TaskID BIGINT, @UserID BIGINT, @Penalty INT;
        DECLARE cur CURSOR FOR SELECT TaskID, UserID, PenaltyHP FROM NxvVw_OverdueTasks;
        OPEN cur;
        FETCH NEXT FROM cur INTO @TaskID, @UserID, @Penalty;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            BEGIN TRANSACTION;
                UPDATE NxvTasks SET Status = 2, UpdatedAt = SYSDATETIME() WHERE TaskID = @TaskID;

                INSERT INTO NxvTaskHistory (TaskID, UserID, ActionType)
                VALUES (@TaskID, @UserID, 'FAIL');

                IF NOT EXISTS (SELECT 1 FROM Users WHERE UserID = @UserID AND IsDead = 1)
                BEGIN
                    -- [V2.2] Khiên chắn cấp NHÂN VẬT (NxvCharacters.IsShieldActive - từ
                    -- skill/pet/item, khác với IsShielded cấp-task đã lọc sẵn ở NxvVw_OverdueTasks):
                    -- nếu đang bật thì miễn toàn bộ sát thương lần trễ hạn này và tiêu hao khiên.
                    DECLARE @CharShielded BIT = 0;
                    SELECT @CharShielded = IsShieldActive FROM NxvCharacters WHERE UserID = @UserID;

                    IF @CharShielded = 1
                    BEGIN
                        UPDATE NxvCharacters
                        SET IsShieldActive = 0, UpdatedAt = SYSDATETIME()
                        WHERE UserID = @UserID;

                        INSERT INTO NxvStatLogs (UserID, LogType, Delta, SourceType, SourceID)
                        VALUES (@UserID, 'HP', 0, 'ShieldBlockedDeadlineFail', @TaskID);
                    END
                    ELSE
                    BEGIN
                        -- Lấy bùa lợi giảm sát thương của trang bị đang mang
                        DECLARE @DmgReduce DECIMAL(5,4) = 0;
                        SELECT @DmgReduce = ISNULL(TotalDmgReduce, 0) FROM NxvVw_UserTotalBuffs WHERE UserID = @UserID;

                        -- Tính toán lượng máu thực tế bị trừ sau khi đã giảm trừ sát thương
                        DECLARE @ActualPenalty INT = @Penalty - FLOOR(@Penalty * @DmgReduce);
                        IF @ActualPenalty < 1 SET @ActualPenalty = 1;

                        UPDATE NxvCharacters
                        SET CurrentHP = CASE WHEN CurrentHP - @ActualPenalty <= 0 THEN 0 ELSE CurrentHP - @ActualPenalty END,
                            UpdatedAt = SYSDATETIME()
                        WHERE UserID = @UserID;

                        INSERT INTO NxvStatLogs (UserID, LogType, Delta, SourceType, SourceID)
                        VALUES (@UserID, 'HP', -@ActualPenalty, 'DeadlineFail', @TaskID);

                        -- Trigger Death State nếu cạn sạch HP
                        IF EXISTS (SELECT 1 FROM NxvCharacters WHERE UserID = @UserID AND CurrentHP = 0)
                            UPDATE Users SET IsDead = 1 WHERE UserID = @UserID;
                    END
                END
            COMMIT TRANSACTION;
            FETCH NEXT FROM cur INTO @TaskID, @UserID, @Penalty;
        END
        CLOSE cur; DEALLOCATE cur;
    END TRY
    BEGIN CATCH
        IF CURSOR_STATUS('global', 'cur') >= 0 BEGIN CLOSE cur; DEALLOCATE cur; END
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- [V2.1 - PHỤC HỒI] Kết thúc phiên Tập trung Focus/Zen Mode, tính thưởng Gold/EXP
CREATE OR ALTER PROCEDURE NxvSp_CompleteFocusSession
    @UserID BIGINT,
    @DurationMinutes INT
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM Users WHERE UserID = @UserID AND IsDead = 1)
        THROW 50200, N'Nhân vật đang chết, không thể tích lũy thời gian tập trung.', 1;

    -- Quy đổi: Cứ 10 phút tập trung đổi được 10 EXP và 5 Gold
    DECLARE @BaseEXP INT = (@DurationMinutes / 10) * 10;
    DECLARE @BaseGold INT = (@DurationMinutes / 10) * 5;

    IF @BaseEXP > 0
    BEGIN
        BEGIN TRY
            BEGIN TRANSACTION;
                DECLARE @NewFocusLogID BIGINT;
                INSERT INTO NxvFocusLogs (UserID, DurationMinutes, RewardEXP, RewardGold)
                VALUES (@UserID, @DurationMinutes, @BaseEXP, @BaseGold);
                SET @NewFocusLogID = SCOPE_IDENTITY();

                UPDATE NxvCharacters
                SET CurrentEXP = CurrentEXP + @BaseEXP, Gold = Gold + @BaseGold, UpdatedAt = SYSDATETIME()
                WHERE UserID = @UserID;

                INSERT INTO NxvStatLogs (UserID, LogType, Delta, SourceType, SourceID)
                VALUES (@UserID, 'EXP', @BaseEXP, 'FocusMode', @NewFocusLogID),
                       (@UserID, 'Gold', @BaseGold, 'FocusMode', @NewFocusLogID);
            COMMIT TRANSACTION;
        END TRY
        BEGIN CATCH
            IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
            THROW;
        END CATCH
    END
END;
GO

-- ============================================================
-- SEED DATA (Mẫu khởi tạo hệ thống)
-- ============================================================

INSERT INTO NxvLevelConfig (Level, EXPRequired, MaxHP, MaxMP) VALUES
(1, 100, 100, 50),
(2, 250, 120, 60),
(3, 500, 140, 70),
(4, 850, 160, 80),
(5, 1300, 190, 95),
(6, 1900, 220, 110),
(7, 2600, 250, 125),
(8, 3500, 290, 145),
(9, 4600, 330, 165),
(10, 6000, 400, 200);
GO

INSERT INTO NxvEquipmentSlots (SlotName, SlotKey) VALUES
(N'Vũ khí', 'weapon'), (N'Giáp', 'armor'), (N'Trang sức', 'accessory');
GO

INSERT INTO NxvItems (ItemName, ItemType, EquipSlotID, BuffMaxHP, BuffDeadlineDmgReduce, SellPrice) VALUES
(N'Giáp Kiên Cường Pixel', 0, 2, 50, 0.2000, 100);
INSERT INTO NxvItems (ItemName, ItemType, EquipSlotID, BuffEXPBonus, BuffGoldBonus, SellPrice) VALUES
(N'Kiếm Gỗ Dev Thử Việc', 0, 1, 0.0500, 0.1000, 150);
INSERT INTO NxvItems (ItemName, ItemType, EquipSlotID, BuffMaxMP, SellPrice) VALUES
(N'Nhẫn Tĩnh Tâm Lofi', 0, 3, 30, 80);
INSERT INTO NxvItems (ItemName, ItemType, HealHP, SellPrice) VALUES
(N'Bình Hồi Máu Nhỏ', 1, 25, 20);
GO

INSERT INTO NxvClasses (ClassName, PassiveEXPBonus, PassiveGoldBonus) VALUES
(N'Tech Mage', 0.0500, 0.0200), (N'Physical Knight', 0.0200, 0.0500);
GO

INSERT INTO NxvSkillDefinitions (SkillName, SkillType, MPCost, CooldownSeconds, RequiredLevel, MaxSkillLevel, SkillEffect) VALUES
(N'Chiêu Lướt (Dash)', 0, 20, 86400, 1, 5, 'DASH_24H'),
(N'Khiên Chắn (Shield)', 0, 30, 86400, 2, 5, 'SHIELD_TASK');
GO

INSERT INTO NxvPetSpecies (SpeciesName, ElementType, EvolveDays, PassiveGoldBonus, PassiveDropRateBonus) VALUES
(N'Rồng Lửa Thực Tập', N'Hỏa', 14, 0.0500, 0.0200),
(N'Mèo Máy Lập Trình', N'Lôi', 14, 0.0300, 0.0500);
GO

INSERT INTO NxvRevivalPackages (PackageName, AmountVND) VALUES
(N'Vé Hồi Sinh Cơ Bản', 20000),
(N'Vé Hồi Sinh Hoàng Kim', 50000);
GO

-- [V2.1] Seed mẫu cho Titles & Achievements
INSERT INTO NxvTitles (TitleName, IsExclusive) VALUES
(N'Tân Binh', 0),
(N'Thợ Săn Deadline', 0),
(N'Huyền Thoại GPA 3.6+', 1);
GO

INSERT INTO NxvAchievements (Code, AchievementName, Description, RewardTitleID, RewardGold, RewardEXP) VALUES
(N'FIRST_TASK_DONE', N'Bước Chân Đầu Tiên', N'Hoàn thành task đầu tiên trong hệ thống', 1, 20, 50),
(N'STREAK_7_DAYS', N'Kỷ Luật Thép', N'Duy trì streak 7 ngày liên tục', 2, 100, 200);
GO

PRINT N'=== TRIỂN KHAI MASTER DATABASE V2.2 (ĐÃ VÁ TRIỆT ĐỂ: BASE/EFFECTIVE STAT + SHIELD) HOÀN TẤT ===';
