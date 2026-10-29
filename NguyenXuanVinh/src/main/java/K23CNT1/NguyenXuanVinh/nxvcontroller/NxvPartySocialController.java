package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvCharacterDtos.PublicProfileCardDto;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvPartySocialDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.NxvEpicQuests;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvCharacterService;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvPartySocialService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social-parties")
@RequiredArgsConstructor
@Tag(name = "Module 7: Tổ đội, Tương tác Xã hội & Bảng xếp hạng (Chức năng #91 - #102)", description = "Party CRUD, Share Task, Trừ máu chung cả đội, Chia thưởng Boss, Chat nhóm, Bảng xếp hạng & Kết bạn")
public class NxvPartySocialController {

    private final NxvPartySocialService partySocialService;
    private final NxvCharacterService characterService;

    // --- QUAN LY TO DOI (PARTY) (#91 -> #94) ---
    @Operation(summary = "[#91] Khởi tạo Tổ đội (Party) mới")
    @PostMapping("/parties")
    public ApiResponse<PartyDetailDto> createParty(@Valid @RequestBody CreatePartyRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Khởi tạo Tổ đội (Party) thành công!", partySocialService.createParty(userId, request));
    }

    @Operation(summary = "[#91B] Xem danh sách các Party mà tôi đang tham gia hoặc được mời")
    @GetMapping("/parties/me")
    public ApiResponse<List<PartyDetailDto>> getMyParties() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.getMyParties(userId));
    }

    @Operation(summary = "[#91C] Xem chi tiết thông tin Party và danh sách thành viên")
    @GetMapping("/parties/{partyId}")
    public ApiResponse<PartyDetailDto> getPartyDetail(@PathVariable Long partyId) {
        return ApiResponse.ok(partySocialService.getPartyDetail(partyId));
    }

    @Operation(summary = "[#92] Mời người chơi khác vào Party (Qua targetUserId hoặc targetUsername)")
    @PostMapping("/parties/{partyId}/invite")
    public ApiResponse<PartyDetailDto> inviteMember(@PathVariable Long partyId,
                                                    @RequestBody InviteMemberRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã gửi lời mời gia nhập Party!", partySocialService.invitePlayerToParty(userId, partyId, request));
    }

    @Operation(summary = "[#93] Chấp nhận hoặc Từ chối lời mời vào Party (accept = true/false)")
    @PostMapping("/parties/{partyId}/respond-invite")
    public ApiResponse<Map<String, Object>> respondInvite(@PathVariable Long partyId,
                                                          @RequestParam(defaultValue = "true") boolean accept) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã phản hồi lời mời vào Party!", partySocialService.respondToPartyInvite(userId, partyId, accept));
    }

    @Operation(summary = "[#94A] Đội trưởng kích thành viên khỏi Party")
    @DeleteMapping("/parties/{partyId}/members/{targetUserId}")
    public ApiResponse<PartyDetailDto> kickMember(@PathVariable Long partyId,
                                                  @PathVariable Long targetUserId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã kích thành viên khỏi Party!", partySocialService.kickMember(userId, partyId, targetUserId));
    }

    @Operation(summary = "[#94B] Rời khỏi Party")
    @PostMapping("/parties/{partyId}/leave")
    public ApiResponse<Map<String, Object>> leaveParty(@PathVariable Long partyId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.leaveParty(userId, partyId));
    }

    // --- SHARE TASK, PHAT SAT THUONG CA DOI & DANH BOSS (#95, #96, #97) ---
    @Operation(summary = "[#95] Share Task: Tạo và giao Task chung cho toàn bộ thành viên trong Party")
    @PostMapping("/parties/{partyId}/shared-tasks")
    public ApiResponse<Map<String, Object>> createSharedTask(@PathVariable Long partyId,
                                                             @Valid @RequestBody CreatePartySharedTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã tạo và phân phối Task chung cho toàn Party!", partySocialService.createPartySharedTask(userId, partyId, request));
    }

    @Operation(summary = "[#96] Thuật toán Party: Một người Fail Task chia sẻ -> Tính sát thương trừ HP toàn bộ thành viên trong đội")
    @PostMapping("/parties/{partyId}/shared-tasks/{failedTaskId}/trigger-team-penalty")
    public ApiResponse<Map<String, Object>> triggerTeamPenalty(@PathVariable Long partyId,
                                                               @PathVariable Long failedTaskId) {
        return ApiResponse.ok("Đã áp dụng hình phạt trừ máu liên đới cho toàn Party!", partySocialService.triggerSharedTaskTeamPenalty(partyId, failedTaskId));
    }

    @Operation(summary = "[#97A] Tạo Boss / Epic Quest chung cho toàn Party")
    @PostMapping("/parties/{partyId}/boss-quests")
    public ApiResponse<NxvEpicQuests> createBossQuest(@PathVariable Long partyId,
                                                      @Valid @RequestBody CreatePartyBossQuestRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã triệu hồi Boss / Dự án chung cho Party!", partySocialService.createPartyBossQuest(userId, partyId, request));
    }

    @Operation(summary = "[#97B] Hoàn thành Boss chung và chia phần thưởng (Vàng, EXP, Level Up) cho cả đội")
    @PostMapping("/parties/{partyId}/boss-quests/{epicQuestId}/complete")
    public ApiResponse<Map<String, Object>> completeBossQuest(@PathVariable Long partyId,
                                                              @PathVariable Long epicQuestId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Tiêu diệt Boss thành công! Đã chia phần thưởng cho toàn đội.", partySocialService.completePartyBossAndDistributeRewards(userId, partyId, epicQuestId));
    }

    // --- CHAT NOI BO PARTY (#98) ---
    @Operation(summary = "[#98A] Gửi tin nhắn vào phòng Chat nội bộ của Party")
    @PostMapping("/parties/{partyId}/messages")
    public ApiResponse<PartyMessageDto> sendMessage(@PathVariable Long partyId,
                                                    @Valid @RequestBody SendPartyMessageRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã gửi tin nhắn!", partySocialService.sendPartyMessage(userId, partyId, request));
    }

    @Operation(summary = "[#98B] Xem lịch sử tin nhắn Chat nội bộ của Party")
    @GetMapping("/parties/{partyId}/messages")
    public ApiResponse<List<PartyMessageDto>> getMessages(@PathVariable Long partyId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.getPartyMessages(userId, partyId));
    }

    // --- BANG XEP HANG & CHIA SE THE NHAN VAT (#99, #100, #101) ---
    @Operation(summary = "[#99] Bảng xếp hạng (Leaderboard) Cấp độ nhân vật cao nhất toàn server")
    @GetMapping("/leaderboards/levels")
    public ApiResponse<List<LeaderboardEntryDto>> getLevelLeaderboard() {
        return ApiResponse.ok(partySocialService.getLevelLeaderboard());
    }

    @Operation(summary = "[#100A] Bảng xếp hạng người cày nhiều Vàng nhất (period = ALL / WEEK / MONTH)")
    @GetMapping("/leaderboards/gold")
    public ApiResponse<List<LeaderboardEntryDto>> getGoldLeaderboard(@RequestParam(defaultValue = "ALL") String period) {
        return ApiResponse.ok(partySocialService.getGoldLeaderboard(period));
    }

    @Operation(summary = "[#100B] Bảng xếp hạng người hoàn thành nhiều Task nhất (period = ALL / WEEK / MONTH)")
    @GetMapping("/leaderboards/tasks")
    public ApiResponse<List<LeaderboardEntryDto>> getTaskLeaderboard(@RequestParam(defaultValue = "ALL") String period) {
        return ApiResponse.ok(partySocialService.getTaskLeaderboard(period));
    }

    @Operation(summary = "[#101] Khoe thẻ thành tựu / thẻ nhân vật tĩnh lên mạng xã hội (Share Profile Card)")
    @GetMapping("/share-card/{targetUserId}")
    public ApiResponse<PublicProfileCardDto> shareProfileCard(@PathVariable Long targetUserId) {
        return ApiResponse.ok(characterService.getPublicProfileCard(targetUserId));
    }

    // --- HE THONG KET BAN (FRIENDSHIPS) (#102) ---
    @Operation(summary = "[#102A] Gửi lời mời kết bạn (Qua targetUserId hoặc targetUsername)")
    @PostMapping("/friends/request")
    public ApiResponse<FriendshipDto> sendFriendRequest(@RequestBody FriendRequestDto request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã gửi lời mời kết bạn!", partySocialService.sendFriendRequest(userId, request));
    }

    @Operation(summary = "[#102B] Chấp nhận hoặc Từ chối lời mời kết bạn (accept = true/false)")
    @PostMapping("/friends/{friendshipId}/respond")
    public ApiResponse<FriendshipDto> respondFriendRequest(@PathVariable Long friendshipId,
                                                           @RequestParam(defaultValue = "true") boolean accept) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã phản hồi lời mời kết bạn!", partySocialService.respondToFriendRequest(userId, friendshipId, accept));
    }

    @Operation(summary = "[#102C] Xem danh sách Bạn bè và Lời mời kết bạn (status = 0: Chờ duyệt, 1: Bạn bè)")
    @GetMapping("/friends")
    public ApiResponse<List<FriendshipDto>> getMyFriends(@RequestParam(required = false) Integer status) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.getMyFriendships(userId, status));
    }
}