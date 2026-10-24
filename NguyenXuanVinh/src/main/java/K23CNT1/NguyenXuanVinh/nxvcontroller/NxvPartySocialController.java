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
@Tag(name = "Module 7: Tá»• Ä‘á»™i, TÆ°Æ¡ng tÃ¡c XÃ£ há»™i & Báº£ng xáº¿p háº¡ng (Chá»©c nÄƒng #91 - #102)", description = "Party CRUD, Share Task, Trá»« mÃ¡u chung cáº£ Ä‘á»™i, Chia thÆ°á»Ÿng Boss, Chat nhÃ³m, Báº£ng xáº¿p háº¡ng & Káº¿t báº¡n")
public class NxvPartySocialController {

    private final NxvPartySocialService partySocialService;
    private final NxvCharacterService characterService;

    // --- QUAN LY TO DOI (PARTY) (#91 -> #94) ---
    @Operation(summary = "[#91] Khá»Ÿi táº¡o Tá»• Ä‘á»™i (Party) má»›i")
    @PostMapping("/parties")
    public ApiResponse<PartyDetailDto> createParty(@Valid @RequestBody CreatePartyRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Khá»Ÿi táº¡o Tá»• Ä‘á»™i (Party) thÃ nh cÃ´ng!", partySocialService.createParty(userId, request));
    }

    @Operation(summary = "[#91B] Xem danh sÃ¡ch cÃ¡c Party mÃ  tÃ´i Ä‘ang tham gia hoáº·c Ä‘Æ°á»£c má»i")
    @GetMapping("/parties/me")
    public ApiResponse<List<PartyDetailDto>> getMyParties() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.getMyParties(userId));
    }

    @Operation(summary = "[#91C] Xem chi tiáº¿t thÃ´ng tin Party vÃ  danh sÃ¡ch thÃ nh viÃªn")
    @GetMapping("/parties/{partyId}")
    public ApiResponse<PartyDetailDto> getPartyDetail(@PathVariable Long partyId) {
        return ApiResponse.ok(partySocialService.getPartyDetail(partyId));
    }

    @Operation(summary = "[#92] Má»i ngÆ°á»i chÆ¡i khÃ¡c vÃ o Party (Qua targetUserId hoáº·c targetUsername)")
    @PostMapping("/parties/{partyId}/invite")
    public ApiResponse<PartyDetailDto> inviteMember(@PathVariable Long partyId,
                                                    @RequestBody InviteMemberRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ gá»­i lá»i má»i gia nháº­p Party!", partySocialService.invitePlayerToParty(userId, partyId, request));
    }

    @Operation(summary = "[#93] Cháº¥p nháº­n hoáº·c Tá»« chá»‘i lá»i má»i vÃ o Party (accept = true/false)")
    @PostMapping("/parties/{partyId}/respond-invite")
    public ApiResponse<Map<String, Object>> respondInvite(@PathVariable Long partyId,
                                                          @RequestParam(defaultValue = "true") boolean accept) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ pháº£n há»“i lá»i má»i vÃ o Party!", partySocialService.respondToPartyInvite(userId, partyId, accept));
    }

    @Operation(summary = "[#94A] Äá»™i trÆ°á»Ÿng kÃ­ch thÃ nh viÃªn khá»i Party")
    @DeleteMapping("/parties/{partyId}/members/{targetUserId}")
    public ApiResponse<PartyDetailDto> kickMember(@PathVariable Long partyId,
                                                  @PathVariable Long targetUserId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ kÃ­ch thÃ nh viÃªn khá»i Party!", partySocialService.kickMember(userId, partyId, targetUserId));
    }

    @Operation(summary = "[#94B] Rá»i khá»i Party")
    @PostMapping("/parties/{partyId}/leave")
    public ApiResponse<Map<String, Object>> leaveParty(@PathVariable Long partyId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.leaveParty(userId, partyId));
    }

    // --- SHARE TASK, PHAT SAT THUONG CA DOI & DANH BOSS (#95, #96, #97) ---
    @Operation(summary = "[#95] Share Task: Táº¡o vÃ  giao Task chung cho toÃ n bá»™ thÃ nh viÃªn trong Party")
    @PostMapping("/parties/{partyId}/shared-tasks")
    public ApiResponse<Map<String, Object>> createSharedTask(@PathVariable Long partyId,
                                                             @Valid @RequestBody CreatePartySharedTaskRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ táº¡o vÃ  phÃ¢n phá»‘i Task chung cho toÃ n Party!", partySocialService.createPartySharedTask(userId, partyId, request));
    }

    @Operation(summary = "[#96] Thuáº­t toÃ¡n Party: Má»™t ngÆ°á»i Fail Task chia sáº» -> TÃ­nh sÃ¡t thÆ°Æ¡ng trá»« HP toÃ n bá»™ thÃ nh viÃªn trong Ä‘á»™i")
    @PostMapping("/parties/{partyId}/shared-tasks/{failedTaskId}/trigger-team-penalty")
    public ApiResponse<Map<String, Object>> triggerTeamPenalty(@PathVariable Long partyId,
                                                               @PathVariable Long failedTaskId) {
        return ApiResponse.ok("ÄÃ£ Ã¡p dá»¥ng hÃ¬nh pháº¡t trá»« mÃ¡u liÃªn Ä‘á»›i cho toÃ n Party!", partySocialService.triggerSharedTaskTeamPenalty(partyId, failedTaskId));
    }

    @Operation(summary = "[#97A] Táº¡o Boss / Epic Quest chung cho toÃ n Party")
    @PostMapping("/parties/{partyId}/boss-quests")
    public ApiResponse<NxvEpicQuests> createBossQuest(@PathVariable Long partyId,
                                                      @Valid @RequestBody CreatePartyBossQuestRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ triá»‡u há»“i Boss / Dá»± Ã¡n chung cho Party!", partySocialService.createPartyBossQuest(userId, partyId, request));
    }

    @Operation(summary = "[#97B] HoÃ n thÃ nh Boss chung vÃ  chia pháº§n thÆ°á»Ÿng (VÃ ng, EXP, Level Up) cho cáº£ Ä‘á»™i")
    @PostMapping("/parties/{partyId}/boss-quests/{epicQuestId}/complete")
    public ApiResponse<Map<String, Object>> completeBossQuest(@PathVariable Long partyId,
                                                              @PathVariable Long epicQuestId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("TiÃªu diá»‡t Boss thÃ nh cÃ´ng! ÄÃ£ chia pháº§n thÆ°á»Ÿng cho toÃ n Ä‘á»™i.", partySocialService.completePartyBossAndDistributeRewards(userId, partyId, epicQuestId));
    }

    // --- CHAT NOI BO PARTY (#98) ---
    @Operation(summary = "[#98A] Gá»­i tin nháº¯n vÃ o phÃ²ng Chat ná»™i bá»™ cá»§a Party")
    @PostMapping("/parties/{partyId}/messages")
    public ApiResponse<PartyMessageDto> sendMessage(@PathVariable Long partyId,
                                                    @Valid @RequestBody SendPartyMessageRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ gá»­i tin nháº¯n!", partySocialService.sendPartyMessage(userId, partyId, request));
    }

    @Operation(summary = "[#98B] Xem lá»‹ch sá»­ tin nháº¯n Chat ná»™i bá»™ cá»§a Party")
    @GetMapping("/parties/{partyId}/messages")
    public ApiResponse<List<PartyMessageDto>> getMessages(@PathVariable Long partyId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.getPartyMessages(userId, partyId));
    }

    // --- BANG XEP HANG & CHIA SE THE NHAN VAT (#99, #100, #101) ---
    @Operation(summary = "[#99] Báº£ng xáº¿p háº¡ng (Leaderboard) Cáº¥p Ä‘á»™ nhÃ¢n váº­t cao nháº¥t toÃ n server")
    @GetMapping("/leaderboards/levels")
    public ApiResponse<List<LeaderboardEntryDto>> getLevelLeaderboard() {
        return ApiResponse.ok(partySocialService.getLevelLeaderboard());
    }

    @Operation(summary = "[#100A] Báº£ng xáº¿p háº¡ng ngÆ°á»i cÃ y nhiá»u VÃ ng nháº¥t (period = ALL / WEEK / MONTH)")
    @GetMapping("/leaderboards/gold")
    public ApiResponse<List<LeaderboardEntryDto>> getGoldLeaderboard(@RequestParam(defaultValue = "ALL") String period) {
        return ApiResponse.ok(partySocialService.getGoldLeaderboard(period));
    }

    @Operation(summary = "[#100B] Báº£ng xáº¿p háº¡ng ngÆ°á»i hoÃ n thÃ nh nhiá»u Task nháº¥t (period = ALL / WEEK / MONTH)")
    @GetMapping("/leaderboards/tasks")
    public ApiResponse<List<LeaderboardEntryDto>> getTaskLeaderboard(@RequestParam(defaultValue = "ALL") String period) {
        return ApiResponse.ok(partySocialService.getTaskLeaderboard(period));
    }

    @Operation(summary = "[#101] Khoe tháº» thÃ nh tá»±u / tháº» nhÃ¢n váº­t tÄ©nh lÃªn máº¡ng xÃ£ há»™i (Share Profile Card)")
    @GetMapping("/share-card/{targetUserId}")
    public ApiResponse<PublicProfileCardDto> shareProfileCard(@PathVariable Long targetUserId) {
        return ApiResponse.ok(characterService.getPublicProfileCard(targetUserId));
    }

    // --- HE THONG KET BAN (FRIENDSHIPS) (#102) ---
    @Operation(summary = "[#102A] Gá»­i lá»i má»i káº¿t báº¡n (Qua targetUserId hoáº·c targetUsername)")
    @PostMapping("/friends/request")
    public ApiResponse<FriendshipDto> sendFriendRequest(@RequestBody FriendRequestDto request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ gá»­i lá»i má»i káº¿t báº¡n!", partySocialService.sendFriendRequest(userId, request));
    }

    @Operation(summary = "[#102B] Cháº¥p nháº­n hoáº·c Tá»« chá»‘i lá»i má»i káº¿t báº¡n (accept = true/false)")
    @PostMapping("/friends/{friendshipId}/respond")
    public ApiResponse<FriendshipDto> respondFriendRequest(@PathVariable Long friendshipId,
                                                           @RequestParam(defaultValue = "true") boolean accept) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ pháº£n há»“i lá»i má»i káº¿t báº¡n!", partySocialService.respondToFriendRequest(userId, friendshipId, accept));
    }

    @Operation(summary = "[#102C] Xem danh sÃ¡ch Báº¡n bÃ¨ vÃ  Lá»i má»i káº¿t báº¡n (status = 0: Chá» duyá»‡t, 1: Báº¡n bÃ¨)")
    @GetMapping("/friends")
    public ApiResponse<List<FriendshipDto>> getMyFriends(@RequestParam(required = false) Integer status) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(partySocialService.getMyFriendships(userId, status));
    }
}