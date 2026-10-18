\# TASKTALE / LIFERPG - MASTER TECHNICAL SPECIFICATION (VERSION 2.2)



\## 1. TỔNG QUAN \& CÔNG NGHỆ CỐT LÕI

\- \*\*Dự án:\*\* Nền tảng Gamification Quản Lý Kỷ Luật \& Năng Suất (TaskTale / LifeRPG) - Đồ án tốt nghiệp 9 tín chỉ (122 chức năng / 9 Modules).

\- \*\*Tech Stack:\*\*

&#x20; - \*\*Backend:\*\* Java 21 + Spring Boot 3.4.12 (Spring Data JPA, Spring Security + JWT, OAuth2 Client, JavaMailSender, Springdoc OpenAPI 2.8.5).

&#x20; - \*\*Frontend:\*\* ReactJS.

&#x20; - \*\*Database:\*\* Microsoft SQL Server (`NxvTaskTaleDB` v2.2 - Collation `Vietnamese\_CI\_AS`).



\## 2. QUY TẮC BẮT BUỘC (HARD CONSTRAINTS)

1\. \*\*Quy tắc bảng và Entity `Users`:\*\* Tên bảng là `Users` và tên class JPA Entity bắt buộc là `public class Users` (`Users.java`, CÓ CHỮ `s`).

2\. \*\*Quy tắc đồ họa 2D tĩnh (Static 2D Only):\*\* Nhân vật, Thú cưng, Vật phẩm, Danh hiệu và Banner chỉ dùng ảnh tĩnh 2D. Khi mặc/tháo trang bị (`NxvSp\_EquipItem` / `NxvSp\_UnequipItem`), chỉ tính lại `MaxHP` / `MaxMP` hiệu lực dựa trên `BaseMaxHP` / `BaseMaxMP` + `NxvVw\_UserTotalBuffs`, tuyệt đối KHÔNG làm cơ chế thay đổi ngoại hình (paper-doll).

3\. \*\*Tính năng cấm:\*\* Không làm Hầm ngục Pomodoro hay Vật phẩm nguyền rủa trên giao diện.

4\. \*\*Quy tắc Mapping SQL Server:\*\*

&#x20;  - Giữ nguyên tên bảng và cột PascalCase bằng `PhysicalNamingStrategyStandardImpl`.

&#x20;  - Trong `NxvCharacters`, tách biệt rõ `BaseMaxHP` / `BaseMaxMP` (chỉ số gốc theo Level) và `MaxHP` / `MaxMP` (chỉ số hiệu lực sau khi cộng Buff trang bị).



\## 3. CẤU TRÚC PACKAGE BACKEND (`K23CNT1.NguyenXuanVinh`)

\- `...nxvconfig`: SecurityConfig, JwtAuthenticationFilter, CorsConfig, OpenApiConfig.

\- `...nxventity`: 54 Class Entity bảng + 5 Class View (`@Immutable`).

\- `...nxvenums`: `NxvConstants` và `ErrorCode` (bắt đầy đủ mã lỗi SQL `50010`–`50012`, `50100`–`50104`, `50200`).

\- `...nxvrepository`: 59 JPA Repositories (tích hợp sẵn 7 Stored Procedures).

\- `...nxvdto`: Request/Response DTOs + `ApiResponse<T>`.

\- `...nxvservice`: Business Logic \& Transaction Management.

\- `...nxvcontroller`: REST API Controllers cho 9 Modules.

\- `...nxvexception`: `AppException`, `GlobalExceptionHandler`.

\- `...nxvutils`: JWT Utils, Mail Utils, Spring Scheduler/Cronjob.



\## 4. DANH SÁCH 54 BẢNG, 5 VIEWS \& 7 STORED PROCEDURES (SCHEMA V2.2)

\- \*\*54 Bảng:\*\*

&#x20; - \*Nhóm 1 (7 bảng Base):\* `Users`, `NxvLevelConfig` (PK không tự tăng), `NxvTags`, `NxvEquipmentSlots`, `NxvClasses`, `NxvPetSpecies`, `NxvTitles`.

&#x20; - \*Nhóm 2 (6 bảng Tài khoản \& Nhân vật):\* `NxvOAuthProviders`, `NxvOtpTokens`, `NxvUserSessions`, `NxvCharacters` (có `BaseMaxHP`, `BaseMaxMP`, `MaxHP`, `MaxMP`, `SkillPoints`, `IsShieldActive`, `CurrentTitleID`), `NxvUserTitles`, `NxvStatLogs`.

&#x20; - \*Nhóm 3 (4 bảng Xã hội \& Tổ đội):\* `NxvParties`, `NxvPartyMembers`, `NxvPartyMessages`, `NxvFriendships`.

&#x20; - \*Nhóm 4 (7 bảng Nhiệm vụ \& GPA):\* `NxvEpicQuests`, `NxvTasks`, `NxvTaskTemplates`, `NxvSubTasks`, `NxvTaskTags`, `NxvGPASubjects`, `NxvTaskHistory`.

&#x20; - \*Nhóm 5 (6 bảng Kỹ năng \& Class):\* `NxvUserUnlockedClasses`, `NxvClassChangeLogs`, `NxvSkillDefinitions`, `NxvUserSkills`, `NxvSkillUsageLogs`, `NxvUserTagMastery`.

&#x20; - \*Nhóm 6 (3 bảng Thú cưng \& AI):\* `NxvUserPets`, `NxvPetInteractionLogs`, `NxvPetDex`.

&#x20; - \*Nhóm 7 (12 bảng Gacha, Túi đồ, Chợ \& Thành tựu):\* `NxvItems`, `NxvUserInventoryConfig` (PK `UserID` không tự tăng), `NxvInventory`, `NxvEquippedItems`, `NxvGachaBanners`, `NxvGachaDropRates`, `NxvUserPityCounters`, `NxvGachaHistory`, `NxvMarketListings`, `NxvMarketTransactions`, `NxvAchievements`, `NxvUserAchievements`.

&#x20; - \*Nhóm 8 (5 bảng Thông báo, IRL \& Admin):\* `NxvNotifications`, `NxvJournals`, `NxvFocusLogs`, `NxvUserReports`, `NxvAIConfig`.

&#x20; - \*Nhóm 9 (4 bảng Thanh toán \& Sinh tử):\* `NxvRevivalPackages`, `NxvPaymentOrders`, `NxvWebhookLogs`, `NxvBillingTransactions`.

\- \*\*5 Views:\*\* `NxvVw\_UserTotalBuffs`, `NxvVw\_LevelLeaderboard`, `NxvVw\_GoldLeaderboard`, `NxvVw\_TaskLeaderboard`, `NxvVw\_OverdueTasks`.

\- \*\*7 Stored Procedures:\*\*

&#x20; 1. `NxvSp\_CalculateGPA (@EpicQuestID)`

&#x20; 2. `NxvSp\_CompleteTask (@TaskID, @UserID)`

&#x20; 3. `NxvSp\_EquipItem (@UserID, @ItemID)`

&#x20; 4. `NxvSp\_UnequipItem (@UserID, @SlotID)`

&#x20; 5. `NxvSp\_UseConsumable (@UserID, @ItemID)`

&#x20; 6. `NxvSp\_CronDailyDeadlineCheck`

&#x20; 7. `NxvSp\_CompleteFocusSession (@UserID, @DurationMinutes)`

