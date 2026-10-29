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
@Tag(name = "Module 5: Thú cưng Đồng hành AI & Nhật ký IRL (Chức năng #57 - #71)", description = "Ấp trứng, Nở & Tiến hóa Pet theo Streak, Cho ăn, AI Pet khịa/động viên, Ngủ đông, Pet Dex & Nhật ký IRL")
public class NxvPetIrlController {

    private final NxvPetIrlService petIrlService;

    @Operation(summary = "[#57] Nhận Trứng thú cưng và đưa vào lồng ấp (Stage = 0)")
    @PostMapping("/pets/adopt-egg")
    public ApiResponse<UserPetDto> adoptEgg(@RequestBody(required = false) AdoptEggRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã nhận Trứng thú cưng và đưa vào lồng ấp!", petIrlService.adoptAndIncubateEgg(userId, request));
    }

    @Operation(summary = "[#58] Ấp nở Trứng thành Thú cưng (Stage 0 -> Stage 1, mở khóa vào Pet Dex)")
    @PostMapping("/pets/{userPetId}/hatch")
    public ApiResponse<UserPetDto> hatchEgg(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Chúc mừng! Trứng đã nở thành Thú cưng đồng hành!", petIrlService.hatchPetEgg(userId, userPetId));
    }

    @Operation(summary = "[#59] Đặt tên và chọn Hệ phái (ElementType) cho Thú cưng")
    @PutMapping("/pets/{userPetId}/rename-element")
    public ApiResponse<UserPetDto> renamePet(@PathVariable Long userPetId,
                                             @Valid @RequestBody RenameAndElementRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cập nhật tên và hệ phái cho Thú cưng thành công!", petIrlService.renameAndChangeElement(userId, userPetId, request));
    }

    @Operation(summary = "[#60, #61, #64] Xem danh sách Thú cưng hiện có (Ảnh 2D tĩnh, Độ no, Thân thiết & Chỉ số Buff)")
    @GetMapping("/pets/me")
    public ApiResponse<List<UserPetDto>> getMyPets() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyPets(userId));
    }

    @Operation(summary = "[#65] Chọn / Đổi Thú cưng đang đồng hành trên Dashboard (Kích hoạt Buff Vàng & Gacha)")
    @PostMapping("/pets/{userPetId}/activate")
    public ApiResponse<UserPetDto> activatePet(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã chọn Thú cưng đồng hành!", petIrlService.setActiveCompanionPet(userId, userPetId));
    }

    @Operation(summary = "[#62] Cho Thú cưng ăn bằng Vàng hoặc Vật phẩm tiêu hao (Tăng Độ no, Thân thiết & Đánh thức khỏi ngủ đông)")
    @PostMapping("/pets/{userPetId}/feed")
    public ApiResponse<Map<String, Object>> feedPet(@PathVariable Long userPetId,
                                                    @RequestBody(required = false) FeedPetRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Cho Thú cưng ăn thành công!", petIrlService.feedPet(userId, userPetId, request));
    }

    @Operation(summary = "[#63] Tiến hóa Thú cưng lên hình thái Trưởng thành (Đổi URL ảnh 2D tĩnh khi đủ mốc Streak/Thân thiết)")
    @PostMapping("/pets/{userPetId}/evolve")
    public ApiResponse<UserPetDto> evolvePet(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Thú cưng đã tiến hóa lên hình thái Trưởng thành!", petIrlService.evolvePet(userId, userPetId));
    }

    @Operation(summary = "[#70] Vuốt ve / Click vào ảnh Pet để tăng Thân thiết và nhận lời thoại ngẫu nhiên")
    @PostMapping("/pets/{userPetId}/interact")
    public ApiResponse<Map<String, Object>> interactPet(@PathVariable Long userPetId) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.patAndInteractWithPet(userId, userPetId));
    }

    @Operation(summary = "[#66, #67, #68] AI Pet Chatbot: Phân tích dữ liệu Task để 'Khịa' khi lười biếng hoặc 'Động viên' khi giữ Streak tốt")
    @PostMapping("/pets/{userPetId}/ai-chat")
    public ApiResponse<Map<String, Object>> aiChatWithPet(@PathVariable Long userPetId,
                                                          @RequestBody(required = false) AIChatPetRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        String prompt = (request != null) ? request.getUserPrompt() : null;
        return ApiResponse.ok(petIrlService.askAiPetCompanion(userId, userPetId, prompt));
    }

    @Operation(summary = "[#66B] Xem lịch sử tương tác và lời thoại AI của Thú cưng")
    @GetMapping("/pets/interaction-logs")
    public ApiResponse<List<PetInteractionLogDto>> getInteractionLogs() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getPetInteractionLogs(userId));
    }

    @Operation(summary = "[#69] Kích hoạt quét trạng thái Ngủ đông (Hibernation) nếu người chơi bỏ bê hoặc Pet đói về 0")
    @PostMapping("/pets/cron/check-hibernation")
    public ApiResponse<Map<String, Object>> checkHibernation() {
        return ApiResponse.ok("Đã kiểm tra trạng thái ngủ đông của toàn bộ Thú cưng!", petIrlService.checkAndProcessPetHibernationCron());
    }

    @Operation(summary = "[#71] Xem Bộ sưu tập Thú cưng (Pet Dex) gồm các ảnh 2D tĩnh đã mở khóa")
    @GetMapping("/pet-dex")
    public ApiResponse<List<PetDexEntryDto>> getMyPetDex() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyPetDex(userId));
    }

    // --- IRL JOURNALS & FOCUS SESSION ---
    @Operation(summary = "[IRL] Viết Nhật ký hàng ngày & nhận phản hồi từ AI")
    @PostMapping("/journals")
    public ApiResponse<JournalDto> createJournal(@Valid @RequestBody CreateJournalRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Đã lưu nhật ký hàng ngày!", petIrlService.createJournalEntry(userId, request));
    }

    @Operation(summary = "[IRL] Xem danh sách Nhật ký đã viết")
    @GetMapping("/journals")
    public ApiResponse<List<JournalDto>> getMyJournals() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyJournals(userId));
    }

    @Operation(summary = "[IRL] Ghi nhận hoàn thành Phiên Tập trung (Gọi NxvSp_CompleteFocusSession nhận EXP & Gold)")
    @PostMapping("/focus-sessions/complete")
    public ApiResponse<Map<String, Object>> completeFocusSession(@Valid @RequestBody CompleteFocusRequest request) {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok("Hoàn thành phiên tập trung và nhận thưởng thành công!", petIrlService.completeFocusSession(userId, request));
    }

    @Operation(summary = "[IRL] Xem lịch sử các Phiên Tập trung")
    @GetMapping("/focus-sessions")
    public ApiResponse<List<FocusLogDto>> getMyFocusLogs() {
        Long userId = NxvSecurityUtils.getCurrentUserId();
        return ApiResponse.ok(petIrlService.getMyFocusLogs(userId));
    }
}