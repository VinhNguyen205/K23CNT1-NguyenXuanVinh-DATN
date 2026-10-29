package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvPetIrlDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvPetIrlService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvPetSpeciesRepository petSpeciesRepository;
    private final NxvUserPetsRepository userPetsRepository;
    private final NxvPetInteractionLogsRepository petInteractionLogsRepository;
    private final NxvPetDexRepository petDexRepository;
    private final NxvInventoryRepository inventoryRepository;
    private final NxvTasksRepository tasksRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;
    private final NxvJournalsRepository journalsRepository;
    private final NxvFocusLogsRepository focusLogsRepository;
    private final NxvAIConfigRepository aiConfigRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    public static final long FEED_PET_GOLD_COST = 25L;

    // =========================================================================
    // PHAN 1: AP TRUNG, NO RA PET, DAT TEN, TIEN HOA & DONG HANH (#57 -> #65)
    // =========================================================================

    // Chuc nang #57: Nhan Trung va Dua vao long ap (Stage = 0)
    @Transactional
    public UserPetDto adoptAndIncubateEgg(Long userId, AdoptEggRequest req) {
        Users user = getUserAlive(userId);
        List<NxvPetSpecies> allSpecies = petSpeciesRepository.findAll();
        if (allSpecies.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND, "Chưa có dữ liệu loài Thú cưng (NxvPetSpecies) trong hệ thống!");
        }

        NxvPetSpecies species;
        if (req != null && req.getSpeciesId() != null) {
            species = petSpeciesRepository.findById(req.getSpeciesId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy loài Thú cưng ID: " + req.getSpeciesId()));
        } else {
            species = allSpecies.get(RANDOM.nextInt(allSpecies.size()));
        }

        // Neu nguoi choi co vat pham Trung thu cung (ItemType = 4) trong tui do thi uu tien tieu thu 1 qua
        inventoryRepository.findByUser_UserId(userId).stream()
                .filter(inv -> inv.getQuantity() > 0 && Objects.equals(inv.getItem().getItemType(), NxvConstants.ITEM_PET_EGG))
                .findFirst()
                .ifPresent(eggInv -> {
                    eggInv.setQuantity(eggInv.getQuantity() - 1);
                    inventoryRepository.save(eggInv);
                });

        String eggImg = (species.getEggImageUrl() != null && !species.getEggImageUrl().isBlank())
                ? species.getEggImageUrl() : "/images/pets/egg_default_static.png";

        String petName = (req != null && req.getPetName() != null && !req.getPetName().isBlank())
                ? req.getPetName().trim() : "Trứng " + species.getSpeciesName();
        String element = (req != null && req.getElementType() != null && !req.getElementType().isBlank())
                ? req.getElementType().trim() : species.getElementType();

        boolean hasActivePet = userPetsRepository.findFirstByUser_UserIdAndIsActiveTrue(userId).isPresent();

        NxvUserPets newPet = userPetsRepository.save(NxvUserPets.builder()
                .user(user)
                .species(species)
                .petName(petName)
                .elementType(element)
                .stage(NxvConstants.PET_STAGE_EGG) // 0 = Trung dang trong long ap
                .currentImageUrl(eggImg)
                .hunger(100)
                .affection(50)
                .isActive(!hasActivePet)
                .isHibernating(false)
                .createdAt(LocalDateTime.now())
                .build());

        return toUserPetDto(newPet, getCharacter(userId));
    }

    // Chuc nang #58: Tinh toan thoi gian thuc / chuoi Streak de Trung no ra Pet (Stage 0 -> Stage 1)
    @Transactional
    public UserPetDto hatchPetEgg(Long userId, Long userPetId) {
        Users user = getUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserPets pet = getUserPet(userId, userPetId);

        if (pet.getStage() != NxvConstants.PET_STAGE_EGG) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Thú cưng này đã nở khỏi trứng rồi!");
        }

        NxvPetSpecies species = pet.getSpecies();
        String babyImg = (species.getBabyImageUrl() != null && !species.getBabyImageUrl().isBlank())
                ? species.getBabyImageUrl() : "/images/pets/baby_" + species.getSpeciesId() + "_static.png";

        pet.setStage(NxvConstants.PET_STAGE_BABY); // 1 = Baby
        pet.setCurrentImageUrl(babyImg);
        pet.setHatchedAt(LocalDateTime.now());
        if (pet.getPetName().startsWith("Trứng ")) {
            pet.setPetName(species.getSpeciesName());
        }
        pet = userPetsRepository.save(pet);

        // Chuc nang #71: Mo khoa vao Bo suu tap Pet Dex (NxvPetDex)
        if (!petDexRepository.existsByUser_UserIdAndSpecies_SpeciesId(userId, species.getSpeciesId())) {
            petDexRepository.save(NxvPetDex.builder()
                    .user(user)
                    .species(species)
                    .firstObtainedAt(LocalDateTime.now())
                    .build());
        }

        petInteractionLogsRepository.save(NxvPetInteractionLogs.builder()
                .userPet(pet)
                .interactionType("HATCH")
                .aiMessage("Chào chủ nhân " + c.getCharacterName() + "! Mình vừa nở khỏi lồng ấp, hãy cùng nhau duy trì chuỗi kỷ luật mỗi ngày nhé!")
                .createdAt(LocalDateTime.now())
                .build());

        return toUserPetDto(pet, c);
    }

    // Chuc nang #59: Dat ten va chon he phai cho Pet
    @Transactional
    public UserPetDto renameAndChangeElement(Long userId, Long userPetId, RenameAndElementRequest req) {
        getUserAlive(userId);
        NxvUserPets pet = getUserPet(userId, userPetId);
        pet.setPetName(req.getPetName().trim());
        if (req.getElementType() != null && !req.getElementType().isBlank()) {
            pet.setElementType(req.getElementType().trim());
        }
        pet = userPetsRepository.save(pet);
        return toUserPetDto(pet, getCharacter(userId));
    }

    // Chuc nang #60 & #64: Xem danh sach Pet & Pet dang dong hanh tren Dashboard
    @Transactional(readOnly = true)
    public List<UserPetDto> getMyPets(Long userId) {
        NxvCharacters c = getCharacter(userId);
        return userPetsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(p -> toUserPetDto(p, c))
                .collect(Collectors.toList());
    }

    // Chuc nang #65: Doi Pet dong hanh tu danh sach thu cung hien co
    @Transactional
    public UserPetDto setActiveCompanionPet(Long userId, Long userPetId) {
        getUserAlive(userId);
        NxvUserPets target = getUserPet(userId, userPetId);
        List<NxvUserPets> allMyPets = userPetsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        for (NxvUserPets p : allMyPets) {
            boolean active = Objects.equals(p.getUserPetId(), userPetId);
            if (!Objects.equals(p.getIsActive(), active)) {
                p.setIsActive(active);
                userPetsRepository.save(p);
            }
        }
        target.setIsActive(true);
        return toUserPetDto(target, getCharacter(userId));
    }

    // Chuc nang #61 & #62: Cho Pet an bang Vang hoac Vat pham tieu hao de duy tri Do no & Than thiet
    @Transactional
    public Map<String, Object> feedPet(Long userId, Long userPetId, FeedPetRequest req) {
        Users user = getUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserPets pet = getUserPet(userId, userPetId);

        if (pet.getStage() == NxvConstants.PET_STAGE_EGG) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Trứng đang trong lồng ấp chưa thể ăn! Hãy ấp nở trứng trước.");
        }

        Integer usedItemId = null;
        boolean payWithGold = (req == null || req.getConsumableItemId() == null || Boolean.TRUE.equals(req.getUseGold()));

        if (payWithGold) {
            if (c.getGold() < FEED_PET_GOLD_COST) {
                throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                        "Không đủ Vàng để mua thức ăn cho Pet! Cần " + FEED_PET_GOLD_COST + " Gold.");
            }
            c.setGold(c.getGold() - FEED_PET_GOLD_COST);
            c.setUpdatedAt(LocalDateTime.now());
            charactersRepository.save(c);

            statLogsRepository.save(NxvStatLogs.builder()
                    .user(user)
                    .logType("Gold")
                    .delta(-FEED_PET_GOLD_COST)
                    .sourceType("FeedPet")
                    .sourceId(userPetId)
                    .createdAt(LocalDateTime.now())
                    .build());
        } else {
            NxvInventory inv = inventoryRepository.findByUser_UserIdAndItem_ItemId(userId, req.getConsumableItemId())
                    .filter(i -> i.getQuantity() > 0)
                    .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY, "Không có vật phẩm này trong túi đồ để cho Pet ăn!"));
            inv.setQuantity(inv.getQuantity() - 1);
            inventoryRepository.save(inv);
            usedItemId = inv.getItem().getItemId();
        }

        boolean wokeUpFromHibernation = Boolean.TRUE.equals(pet.getIsHibernating());
        pet.setHunger(Math.min(100, pet.getHunger() + 35));
        pet.setAffection(Math.min(100, pet.getAffection() + 10));
        pet.setIsHibernating(false);
        pet = userPetsRepository.save(pet);

        String msg = wokeUpFromHibernation
                ? "Nhoàm nhoàm! " + pet.getPetName() + " đã tỉnh giấc sau kỳ ngủ đông và phục hồi toàn bộ Buff đồng hành!"
                : pet.getPetName() + " ăn rất ngon miệng! (Độ no: " + pet.getHunger() + "/100, Thân thiết: " + pet.getAffection() + "/100).";

        petInteractionLogsRepository.save(NxvPetInteractionLogs.builder()
                .userPet(pet)
                .interactionType("FEED")
                .usedItemId(usedItemId)
                .aiMessage(msg)
                .createdAt(LocalDateTime.now())
                .build());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("message", msg);
        res.put("wokeUpFromHibernation", wokeUpFromHibernation);
        res.put("remainingGold", c.getGold());
        res.put("pet", toUserPetDto(pet, c));
        return res;
    }

    // Chuc nang #63: Tinh nang Tien hoa Pet (Tu dong doi URL anh 2D tinh sang dang Truong thanh khi du moc)
    @Transactional
    public UserPetDto evolvePet(Long userId, Long userPetId) {
        getUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserPets pet = getUserPet(userId, userPetId);

        if (pet.getStage() == NxvConstants.PET_STAGE_EGG) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Hãy ấp nở trứng thành Thú non (Stage 1) trước khi tiến hóa!");
        }
        if (pet.getStage() >= NxvConstants.PET_STAGE_ADULT) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Thú cưng này đã ở hình thái Trưởng thành (Stage 2) tối đa rồi!");
        }

        int reqDays = (pet.getSpecies().getEvolveDays() != null && pet.getSpecies().getEvolveDays() > 0)
                ? pet.getSpecies().getEvolveDays() : 14;

        long daysSinceHatch = (pet.getHatchedAt() != null)
                ? Duration.between(pet.getHatchedAt(), LocalDateTime.now()).toDays() : 0L;

        boolean eligibleByStreakOrDays = (c.getStreakDays() >= reqDays) || (daysSinceHatch >= reqDays) || (pet.getAffection() >= 80);
        if (!eligibleByStreakOrDays) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Chưa đủ điều kiện tiến hóa! Cần đạt chuỗi Streak " + reqDays + " ngày (Hiện tại: " + c.getStreakDays()
                            + ") hoặc Độ thân thiết >= 80 (Hiện tại: " + pet.getAffection() + "/100).");
        }

        NxvPetSpecies sp = pet.getSpecies();
        String adultImg = (sp.getAdultImageUrl() != null && !sp.getAdultImageUrl().isBlank())
                ? sp.getAdultImageUrl() : "/images/pets/adult_" + sp.getSpeciesId() + "_static.png";

        pet.setStage(NxvConstants.PET_STAGE_ADULT); // 2 = Truong thanh
        pet.setCurrentImageUrl(adultImg);
        pet.setAffection(100);
        pet = userPetsRepository.save(pet);

        petInteractionLogsRepository.save(NxvPetInteractionLogs.builder()
                .userPet(pet)
                .interactionType("EVOLVE")
                .aiMessage("Ánh sáng tiến hóa bùng nổ! [" + pet.getPetName() + "] đã tiến hóa lên hình thái Trưởng thành nhờ chuỗi kỷ luật của bạn!")
                .createdAt(LocalDateTime.now())
                .build());

        return toUserPetDto(pet, c);
    }

    // Chuc nang #70: Tuong tac vuot ve / click vao anh Pet de nhan loi thoai ngau nhien
    @Transactional
    public Map<String, Object> patAndInteractWithPet(Long userId, Long userPetId) {
        getUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserPets pet = getUserPet(userId, userPetId);

        if (Boolean.TRUE.equals(pet.getIsHibernating())) {
            throw new AppException(ErrorCode.BAD_REQUEST, pet.getPetName() + " đang ngủ đông vì đói! Hãy cho Pet ăn để đánh thức nhé.");
        }

        pet.setAffection(Math.min(100, pet.getAffection() + 5));
        pet = userPetsRepository.save(pet);

        String[] dialogues = new String[]{
                pet.getPetName() + " dụi đầu vào tay " + c.getCharacterName() + ": \"Hôm nay chúng ta làm thêm 1 Task lấy Vàng quay hộp mù đi chủ nhân!\"",
                pet.getPetName() + " vẫy đuôi hào hứng: \"Chỉ số Thân thiết đã tăng lên " + pet.getAffection() + "/100 rồi đó!\"",
                pet.getPetName() + " nhắc nhẹ: \"Đừng quên điểm danh giữ chuỗi Streak " + c.getStreakDays() + " ngày để mình sớm tiến hóa nhé!\"",
                pet.getPetName() + " tỏa ra năng lượng hệ " + pet.getElementType() + ", giúp bạn nhận thêm +" + pet.getSpecies().getPassiveGoldBonus().multiply(BigDecimal.valueOf(100)).intValue() + "% Vàng mỗi khi hoàn thành Task!"
        };
        String chosenLine = dialogues[RANDOM.nextInt(dialogues.length)];

        petInteractionLogsRepository.save(NxvPetInteractionLogs.builder()
                .userPet(pet)
                .interactionType("PET")
                .aiMessage(chosenLine)
                .createdAt(LocalDateTime.now())
                .build());

        return Map.of(
                "userPetId", pet.getUserPetId(),
                "petName", pet.getPetName(),
                "affection", pet.getAffection(),
                "dialogue", chosenLine
        );
    }

    // Chuc nang #71: Xem Bo suu tap (Pet Dex) gom cac anh tinh thu cung da mo khoa
    @Transactional(readOnly = true)
    public List<PetDexEntryDto> getMyPetDex(Long userId) {
        Map<Integer, LocalDateTime> unlockedMap = petDexRepository.findByUser_UserIdOrderByFirstObtainedAtDesc(userId)
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getSpecies().getSpeciesId(),
                        NxvPetDex::getFirstObtainedAt,
                        (a, b) -> a
                ));

        return petSpeciesRepository.findAll().stream()
                .map(sp -> PetDexEntryDto.builder()
                        .speciesId(sp.getSpeciesId())
                        .speciesName(sp.getSpeciesName())
                        .defaultElementType(sp.getElementType())
                        .eggImageUrl(sp.getEggImageUrl() != null ? sp.getEggImageUrl() : "/images/pets/egg_default_static.png")
                        .babyImageUrl(sp.getBabyImageUrl() != null ? sp.getBabyImageUrl() : "/images/pets/baby_" + sp.getSpeciesId() + "_static.png")
                        .adultImageUrl(sp.getAdultImageUrl() != null ? sp.getAdultImageUrl() : "/images/pets/adult_" + sp.getSpeciesId() + "_static.png")
                        .evolveDays(sp.getEvolveDays())
                        .passiveGoldBonus(sp.getPassiveGoldBonus())
                        .passiveDropRateBonus(sp.getPassiveDropRateBonus())
                        .isUnlocked(unlockedMap.containsKey(sp.getSpeciesId()))
                        .firstObtainedAt(unlockedMap.get(sp.getSpeciesId()))
                        .build())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // PHAN 2: AI PET CHATBOT (KHIA KHI LUOI / DONG VIEN KHI CHAM / NGU DONG) (#66->#69)
    // =========================================================================

    // Chuc nang #66, #67, #68: AI Pet tu dong phan tich trang thai Task de Khia hoac Dong vien
    @Transactional
    public Map<String, Object> askAiPetCompanion(Long userId, Long userPetId, String userPrompt) {
        getUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        NxvUserPets pet = getUserPet(userId, userPetId);

        if (Boolean.TRUE.equals(pet.getIsHibernating())) {
            throw new AppException(ErrorCode.BAD_REQUEST, pet.getPetName() + " đang ngủ đông vì bị bỏ bê! Hãy cho Pet ăn trước khi trò chuyện.");
        }

        List<NxvTasks> allTasks = tasksRepository.findByUser_UserIdOrderBySortOrderAscCreatedAtDesc(userId);
        long pendingCount = allTasks.stream().filter(t -> t.getStatus() == NxvConstants.TASK_PENDING).count();
        long completedCount = allTasks.stream().filter(t -> t.getStatus() == NxvConstants.TASK_COMPLETED).count();
        long failedCount = allTasks.stream().filter(t -> t.getStatus() == NxvConstants.TASK_FAILED).count();
        long totalEvaluated = completedCount + failedCount;
        double completionRate = (totalEvaluated > 0) ? (completedCount * 100.0 / totalEvaluated) : 100.0;

        boolean isLazyState = (failedCount > completedCount) || (completionRate < 50.0 && totalEvaluated >= 2) || (pendingCount >= 5 && completedCount == 0);
        String interactionType = isLazyState ? "AI_REMIND" : "AI_PRAISE";

        String aiReply = generateContextualAiPetMessage(pet, c, pendingCount, completedCount, failedCount, completionRate, isLazyState, userPrompt);

        petInteractionLogsRepository.save(NxvPetInteractionLogs.builder()
                .userPet(pet)
                .interactionType(interactionType)
                .aiMessage(aiReply)
                .createdAt(LocalDateTime.now())
                .build());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("userPetId", pet.getUserPetId());
        res.put("petName", pet.getPetName());
        res.put("interactionType", interactionType);
        res.put("mood", isLazyState ? "ROASTING_REMIND (Đang khịa/nhắc nhở)" : "PRAISING_MOTIVATE (Đang khen ngợi/động viên)");
        res.put("taskStats", Map.of(
                "pendingTasks", pendingCount,
                "completedTasks", completedCount,
                "failedTasks", failedCount,
                "completionRatePercent", Math.round(completionRate * 100.0) / 100.0,
                "streakDays", c.getStreakDays()
        ));
        res.put("aiMessage", aiReply);
        return res;
    }

    @Transactional(readOnly = true)
    public List<PetInteractionLogDto> getPetInteractionLogs(Long userId) {
        return petInteractionLogsRepository.findTop100ByUserPet_User_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(l -> PetInteractionLogDto.builder()
                        .logId(l.getLogId())
                        .userPetId(l.getUserPet().getUserPetId())
                        .petName(l.getUserPet().getPetName())
                        .interactionType(l.getInteractionType())
                        .usedItemId(l.getUsedItemId())
                        .aiMessage(l.getAiMessage())
                        .createdAt(l.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Chuc nang #69: Quet tu dong cho Pet ngu dong neu user offline lau hoac Hunger ve 0
    @Transactional
    public Map<String, Object> checkAndProcessPetHibernationCron() {
        List<NxvUserPets> allPets = userPetsRepository.findAll();
        int hibernatedCount = 0;
        LocalDateTime offlineThreshold = LocalDateTime.now().minusDays(3);

        for (NxvUserPets pet : allPets) {
            if (pet.getStage() == NxvConstants.PET_STAGE_EGG || Boolean.TRUE.equals(pet.getIsHibernating())) {
                continue;
            }
            Users owner = pet.getUser();
            int newHunger = Math.max(0, pet.getHunger() - 15);
            pet.setHunger(newHunger);

            boolean ownerOfflineTooLong = (owner.getLastLoginAt() != null && owner.getLastLoginAt().isBefore(offlineThreshold));
            if (newHunger == 0 || ownerOfflineTooLong) {
                pet.setIsHibernating(true);
                hibernatedCount++;

                petInteractionLogsRepository.save(NxvPetInteractionLogs.builder()
                        .userPet(pet)
                        .interactionType("HIBERNATE")
                        .aiMessage(pet.getPetName() + " đã rơi vào trạng thái Ngủ đông do bị bỏ đói hoặc chủ nhân offline quá lâu. Buff thụ động tạm thời bị vô hiệu hóa!")
                        .createdAt(LocalDateTime.now())
                        .build());

                notificationsRepository.save(NxvNotifications.builder()
                        .user(owner)
                        .title("Thú cưng [" + pet.getPetName() + "] đã ngủ đông!")
                        .content("Do bạn bỏ bê nhiệm vụ hoặc chưa cho ăn, " + pet.getPetName() + " đã ngủ đông. Hãy vào cho Pet ăn để khôi phục Buff nhé!")
                        .notificationType("SYSTEM")
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
            userPetsRepository.save(pet);
        }

        return Map.of("totalPetsChecked", allPets.size(), "newlyHibernatedPets", hibernatedCount);
    }

    // =========================================================================
    // PHAN 3: NHAT KY IRL (JOURNALS) & PHIEN TAP TRUNG (FOCUS LOGS SP)
    // =========================================================================

    @Transactional
    public JournalDto createJournalEntry(Long userId, CreateJournalRequest req) {
        Users user = getUserAlive(userId);
        NxvCharacters c = getCharacter(userId);
        String mood = (req.getMoodTag() != null && !req.getMoodTag().isBlank()) ? req.getMoodTag().trim().toUpperCase() : "NEUTRAL";

        String aiFeedback = "Nhật ký của " + c.getCharacterName() + " đã được ghi nhận! Duy trì thói quen nhìn lại bản thân mỗi ngày sẽ giúp bạn giữ vững chuỗi Streak " + c.getStreakDays() + " ngày.";
        NxvJournals journal = journalsRepository.save(NxvJournals.builder()
                .user(user)
                .content(req.getContent().trim())
                .moodTag(mood)
                .aiResponse(aiFeedback)
                .createdAt(LocalDateTime.now())
                .build());

        return JournalDto.builder()
                .journalId(journal.getJournalId())
                .userId(userId)
                .content(journal.getContent())
                .moodTag(journal.getMoodTag())
                .aiResponse(journal.getAiResponse())
                .createdAt(journal.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<JournalDto> getMyJournals(Long userId) {
        return journalsRepository.findTop50ByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(j -> JournalDto.builder()
                        .journalId(j.getJournalId())
                        .userId(userId)
                        .content(j.getContent())
                        .moodTag(j.getMoodTag())
                        .aiResponse(j.getAiResponse())
                        .createdAt(j.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // Goi Stored Procedure NxvSp_CompleteFocusSession (v2.2)
    @Transactional
    public Map<String, Object> completeFocusSession(Long userId, CompleteFocusRequest req) {
        getUserAlive(userId);
        focusLogsRepository.executeCompleteFocusSessionSp(userId, req.getDurationMinutes());

        NxvCharacters c = getCharacter(userId);
        int gainedExp = (req.getDurationMinutes() / 10) * 10;
        int gainedGold = (req.getDurationMinutes() / 10) * 5;

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("durationMinutes", req.getDurationMinutes());
        res.put("expGained", gainedExp);
        res.put("goldGained", gainedGold);
        res.put("currentExp", c.getCurrentExp());
        res.put("currentGold", c.getGold());
        return res;
    }

    @Transactional(readOnly = true)
    public List<FocusLogDto> getMyFocusLogs(Long userId) {
        return focusLogsRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(f -> FocusLogDto.builder()
                        .focusLogId(f.getFocusLogId())
                        .durationMinutes(f.getDurationMinutes())
                        .rewardExp(f.getRewardExp())
                        .rewardGold(f.getRewardGold())
                        .createdAt(f.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Helpers ---
    private String generateContextualAiPetMessage(NxvUserPets pet,
                                                  NxvCharacters c,
                                                  long pending,
                                                  long completed,
                                                  long failed,
                                                  double rate,
                                                  boolean isLazy,
                                                  String userPrompt) {
        String contextSummary = String.format(
                "Chủ nhân: %s (Level %d, HP: %d/%d, Streak: %d ngày). Thú cưng: %s (Hệ %s). Tiến độ Task: Đã xong %d, Trễ hạn %d, Đang chờ %d (Tỷ lệ hoàn thành: %.1f%%).",
                c.getCharacterName(), c.getCurrentLevel(), c.getCurrentHp(), c.getMaxHp(), c.getStreakDays(),
                pet.getPetName(), pet.getElementType(), completed, failed, pending, rate
        );

        // Neu Admin da cau hinh API Key that trong NxvAIConfig thi thu goi LLM API, neu khong co hoac loi thi fallback ve Contextual AI Engine
        Optional<NxvAIConfig> cfgOpt = aiConfigRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc();
        if (cfgOpt.isPresent()) {
            NxvAIConfig cfg = cfgOpt.get();
            if (cfg.getApiKeyEncrypted() != null && cfg.getApiKeyEncrypted().startsWith("AIza")) {
                try {
                    RestTemplate rt = new RestTemplate();
                    String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                            + (cfg.getModelName() != null ? cfg.getModelName() : "gemini-2.5-flash")
                            + ":generateContent?key=" + cfg.getApiKeyEncrypted().trim();
                    String sys = (cfg.getSystemPrompt() != null ? cfg.getSystemPrompt() : "Bạn là thú cưng RPG thông minh.")
                            + " Dữ liệu hiện tại: " + contextSummary
                            + (userPrompt != null && !userPrompt.isBlank() ? " Câu hỏi của chủ nhân: " + userPrompt : "");

                    Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", sys)))));
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<?, ?> resp = rt.postForObject(url, new HttpEntity<>(body, headers), Map.class);
                    if (resp != null && resp.containsKey("candidates")) {
                        List<?> candidates = (List<?>) resp.get("candidates");
                        if (!candidates.isEmpty()) {
                            Map<?, ?> first = (Map<?, ?>) candidates.get(0);
                            Map<?, ?> content = (Map<?, ?>) first.get("content");
                            List<?> parts = (List<?>) content.get("parts");
                            if (!parts.isEmpty()) {
                                Object text = ((Map<?, ?>) parts.get(0)).get("text");
                                if (text != null) return text.toString();
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Contextual AI Fallback (Dam bao chay muot 100% moi luc ke ca khi offline bao ve do an)
        if (isLazy) {
            return "[" + pet.getPetName() + " nhíu mày khịa]: \"Chủ nhân " + c.getCharacterName()
                    + " ơi! Tỷ lệ hoàn thành task đang tụt xuống " + String.format("%.1f%%", rate)
                    + " (Trễ hạn " + failed + " task, còn tồn " + pending + " task chưa đụng tới) kìa! Máu nhân vật chỉ còn "
                    + c.getCurrentHp() + "/" + c.getMaxHp() + " HP thôi, bộ định để nhân vật gục ngã tốn tiền thật quét VietQR mua Vé Hồi Sinh hay sao? Bắt tay xử lý ngay 1 task đi nào!\"";
        } else {
            return "[" + pet.getPetName() + " nhảy lên cổ vũ]: \"Đỉnh quá chủ nhân " + c.getCharacterName()
                    + " ơi! Bạn đã hoàn thành " + completed + " nhiệm vụ với tỷ lệ " + String.format("%.1f%%", rate)
                    + " và giữ vững chuỗi Streak " + c.getStreakDays() + " ngày liên tiếp! Cứ đà kỷ luật thế này thì cả mình và bạn đều sớm max cấp thôi!\"";
        }
    }

    private UserPetDto toUserPetDto(NxvUserPets p, NxvCharacters c) {
        String stLabel = switch (p.getStage() != null ? p.getStage() : 0) {
            case 2 -> "Adult (Trưởng thành)";
            case 1 -> "Baby (Thú non)";
            default -> "Egg (Trứng trong lồng ấp)";
        };
        int reqDays = (p.getSpecies().getEvolveDays() != null) ? p.getSpecies().getEvolveDays() : 14;
        boolean canEvolve = (p.getStage() == 1) && (c.getStreakDays() >= reqDays || p.getAffection() >= 80);
        boolean buffActive = Boolean.TRUE.equals(p.getIsActive()) && !Boolean.TRUE.equals(p.getIsHibernating()) && p.getStage() >= 1;

        return UserPetDto.builder()
                .userPetId(p.getUserPetId())
                .userId(p.getUser().getUserId())
                .speciesId(p.getSpecies().getSpeciesId())
                .speciesName(p.getSpecies().getSpeciesName())
                .petName(p.getPetName())
                .elementType(p.getElementType() != null ? p.getElementType() : p.getSpecies().getElementType())
                .stage(p.getStage())
                .stageLabel(stLabel)
                .currentImageUrl(p.getCurrentImageUrl())
                .hunger(p.getHunger())
                .affection(p.getAffection())
                .isActive(p.getIsActive())
                .isHibernating(p.getIsHibernating())
                .evolveDaysRequired(reqDays)
                .canEvolveNow(canEvolve)
                .passiveGoldBonus(p.getSpecies().getPassiveGoldBonus())
                .passiveDropRateBonus(p.getSpecies().getPassiveDropRateBonus())
                .isBuffCurrentlyActive(buffActive)
                .hatchedAt(p.getHatchedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private Users getUserAlive(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
        return u;
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy nhân vật!"));
    }

    private NxvUserPets getUserPet(Long userId, Long userPetId) {
        return userPetsRepository.findByUserPetIdAndUser_UserId(userPetId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Thú cưng của bạn với ID: " + userPetId));
    }
}