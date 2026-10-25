package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.NxvPetIrlDtos.*;
import K23CNT1.NguyenXuanVinh.nxvservice.NxvPetIrlService;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pets-irl")
@RequiredArgsConstructor
@Tag(name = "Module 5: ThÃº cÆ°ng Äá»“ng hÃ nh AI & Nháº­t kÃ½ IRL (Chá»©c nÄƒng #57 - #71)", description = "áº¤p trá»©ng, Ná»Ÿ & Tiáº¿n hÃ³a Pet theo Streak, Cho Äƒn, AI Pet khá»‹a/Ä‘á»™ng viÃªn, Ngá»§ Ä‘Ã´ng, Pet Dex & Nháº­t kÃ½ IRL")
public class NxvPetIrlController {

    private final NxvPetIrlService petIrlService;

    @Operation(summary = "[#57] Nháº­n Trá»©ng thÃº cÆ°ng vÃ  Ä‘Æ°a vÃ o lá»“ng áº¥p (Stage = 0)")
    @PostMapping("/pets/adopt-egg")
    public ApiResponse<UserPetDto> adoptEgg(@RequestBody(required = false) AdoptEggRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ nháº­n Trá»©ng thÃº cÆ°ng vÃ  Ä‘Æ°a vÃ o lá»“ng áº¥p!", petIrlService.adoptAndIncubateEgg(userId, request));
    }

    @Operation(summary = "[#58] áº¤p ná»Ÿ Trá»©ng thÃ nh ThÃº cÆ°ng (Stage 0 -> Stage 1, má»Ÿ khÃ³a vÃ o Pet Dex)")
    @PostMapping("/pets/{userPetId}/hatch")
    public ApiResponse<UserPetDto> hatchEgg(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ChÃºc má»«ng! Trá»©ng Ä‘Ã£ ná»Ÿ thÃ nh ThÃº cÆ°ng Ä‘á»“ng hÃ nh!", petIrlService.hatchPetEgg(userId, userPetId));
    }

    @Operation(summary = "[#59] Äáº·t tÃªn vÃ  chá»n Há»‡ phÃ¡i (ElementType) cho ThÃº cÆ°ng")
    @PutMapping("/pets/{userPetId}/rename-element")
    public ApiResponse<UserPetDto> renamePet(@PathVariable Long userPetId,
                                             @Valid @RequestBody RenameAndElementRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cáº­p nháº­t tÃªn vÃ  há»‡ phÃ¡i cho ThÃº cÆ°ng thÃ nh cÃ´ng!", petIrlService.renameAndChangeElement(userId, userPetId, request));
    }

    @Operation(summary = "[#60, #61, #64] Xem danh sÃ¡ch ThÃº cÆ°ng hiá»‡n cÃ³ (áº¢nh 2D tÄ©nh, Äá»™ no, ThÃ¢n thiáº¿t & Chá»‰ sá»‘ Buff)")
    @GetMapping("/pets/me")
    public ApiResponse<List<UserPetDto>> getMyPets() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyPets(userId));
    }

    @Operation(summary = "[#65] Chá»n / Äá»•i ThÃº cÆ°ng Ä‘ang Ä‘á»“ng hÃ nh trÃªn Dashboard (KÃ­ch hoáº¡t Buff VÃ ng & Gacha)")
    @PostMapping("/pets/{userPetId}/activate")
    public ApiResponse<UserPetDto> activatePet(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ chá»n ThÃº cÆ°ng Ä‘á»“ng hÃ nh!", petIrlService.setActiveCompanionPet(userId, userPetId));
    }

    @Operation(summary = "[#62] Cho ThÃº cÆ°ng Äƒn báº±ng VÃ ng hoáº·c Váº­t pháº©m tiÃªu hao (TÄƒng Äá»™ no, ThÃ¢n thiáº¿t & ÄÃ¡nh thá»©c khá»i ngá»§ Ä‘Ã´ng)")
    @PostMapping("/pets/{userPetId}/feed")
    public ApiResponse<Map<String, Object>> feedPet(@PathVariable Long userPetId,
                                                    @RequestBody(required = false) FeedPetRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cho ThÃº cÆ°ng Äƒn thÃ nh cÃ´ng!", petIrlService.feedPet(userId, userPetId, request));
    }

    @Operation(summary = "[#63] Tiáº¿n hÃ³a ThÃº cÆ°ng lÃªn hÃ¬nh thÃ¡i TrÆ°á»Ÿng thÃ nh (Äá»•i URL áº£nh 2D tÄ©nh khi Ä‘á»§ má»‘c Streak/ThÃ¢n thiáº¿t)")
    @PostMapping("/pets/{userPetId}/evolve")
    public ApiResponse<UserPetDto> evolvePet(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ThÃº cÆ°ng Ä‘Ã£ tiáº¿n hÃ³a lÃªn hÃ¬nh thÃ¡i TrÆ°á»Ÿng thÃ nh!", petIrlService.evolvePet(userId, userPetId));
    }

    @Operation(summary = "[#70] Vuá»‘t ve / Click vÃ o áº£nh Pet Ä‘á»ƒ tÄƒng ThÃ¢n thiáº¿t vÃ  nháº­n lá»i thoáº¡i ngáº«u nhiÃªn")
    @PostMapping("/pets/{userPetId}/interact")
    public ApiResponse<Map<String, Object>> interactPet(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.patAndInteractWithPet(userId, userPetId));
    }

    @Operation(summary = "[#66, #67, #68] AI Pet Chatbot: PhÃ¢n tÃ­ch dá»¯ liá»‡u Task Ä‘á»ƒ 'Khá»‹a' khi lÆ°á»i biáº¿ng hoáº·c 'Äá»™ng viÃªn' khi giá»¯ Streak tá»‘t")
    @PostMapping("/pets/{userPetId}/ai-chat")
    public ApiResponse<Map<String, Object>> aiChatWithPet(@PathVariable Long userPetId,
                                                          @RequestBody(required = false) AIChatPetRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        String prompt = (request != null) ? request.getUserPrompt() : null;
        return ApiResponse.ok(petIrlService.askAiPetCompanion(userId, userPetId, prompt));
    }

    @Operation(summary = "[#66B] Xem lá»‹ch sá»­ tÆ°Æ¡ng tÃ¡c vÃ  lá»i thoáº¡i AI cá»§a ThÃº cÆ°ng")
    @GetMapping("/pets/interaction-logs")
    public ApiResponse<List<PetInteractionLogDto>> getInteractionLogs() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getPetInteractionLogs(userId));
    }

    @Operation(summary = "[#69] KÃ­ch hoáº¡t quÃ©t tráº¡ng thÃ¡i Ngá»§ Ä‘Ã´ng (Hibernation) náº¿u ngÆ°á»i chÆ¡i bá» bÃª hoáº·c Pet Ä‘Ã³i vá» 0")
    @PostMapping("/pets/cron/check-hibernation")
    public ApiResponse<Map<String, Object>> checkHibernation() {
        return ApiResponse.ok("ÄÃ£ kiá»ƒm tra tráº¡ng thÃ¡i ngá»§ Ä‘Ã´ng cá»§a toÃ n bá»™ ThÃº cÆ°ng!", petIrlService.checkAndProcessPetHibernationCron());
    }

    @Operation(summary = "[#71] Xem Bá»™ sÆ°u táº­p ThÃº cÆ°ng (Pet Dex) gá»“m cÃ¡c áº£nh 2D tÄ©nh Ä‘Ã£ má»Ÿ khÃ³a")
    @GetMapping("/pet-dex")
    public ApiResponse<List<PetDexEntryDto>> getMyPetDex() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyPetDex(userId));
    }

    // --- IRL JOURNALS & FOCUS SESSION ---
    @Operation(summary = "[IRL] Viáº¿t Nháº­t kÃ½ hÃ ng ngÃ y & nháº­n pháº£n há»“i tá»« AI")
    @PostMapping("/journals")
    public ApiResponse<JournalDto> createJournal(@Valid @RequestBody CreateJournalRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("ÄÃ£ lÆ°u nháº­t kÃ½ hÃ ng ngÃ y!", petIrlService.createJournalEntry(userId, request));
    }

    @Operation(summary = "[IRL] Xem danh sÃ¡ch Nháº­t kÃ½ Ä‘Ã£ viáº¿t")
    @GetMapping("/journals")
    public ApiResponse<List<JournalDto>> getMyJournals() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyJournals(userId));
    }

    @Operation(summary = "[IRL] Ghi nháº­n hoÃ n thÃ nh PhiÃªn Táº­p trung (Gá»i NxvSp_CompleteFocusSession nháº­n EXP & Gold)")
    @PostMapping("/focus-sessions/complete")
    public ApiResponse<Map<String, Object>> completeFocusSession(@Valid @RequestBody CompleteFocusRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("HoÃ n thÃ nh phiÃªn táº­p trung vÃ  nháº­n thÆ°á»Ÿng thÃ nh cÃ´ng!", petIrlService.completeFocusSession(userId, request));
    }

    @Operation(summary = "[IRL] Xem lá»‹ch sá»­ cÃ¡c PhiÃªn Táº­p trung")
    @GetMapping("/focus-sessions")
    public ApiResponse<List<FocusLogDto>> getMyFocusLogs() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyFocusLogs(userId));
    }
}