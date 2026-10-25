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
            throw new AppException(ErrorCode.NOT_FOUND, "ChÆ°a cÃ³ dá»¯ liá»‡u loÃ i ThÃº cÆ°ng (NxvPetSpecies) trong há»‡ thá»‘ng!");
        }

        NxvPetSpecies species;
        if (req != null && req.getSpeciesId() != null) {
            species = petSpeciesRepository.findById(req.getSpeciesId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y loÃ i ThÃº cÆ°ng ID: " + req.getSpeciesId()));
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
                ? req.getPetName().trim() : "Trá»©ng " + species.getSpeciesName();
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
            throw new AppException(ErrorCode.BAD_REQUEST, "ThÃº cÆ°ng nÃ y Ä‘Ã£ ná»Ÿ khá»i trá»©ng rá»“i!");
        }

        NxvPetSpecies species = pet.getSpecies();
        String babyImg = (species.getBabyImageUrl() != null && !species.getBabyImageUrl().isBlank())
                ? species.getBabyImageUrl() : "/images/pets/baby_" + species.getSpeciesId() + "_static.png";

        pet.setStage(NxvConstants.PET_STAGE_BABY); // 1 = Baby
        pet.setCurrentImageUrl(babyImg);
        pet.setHatchedAt(LocalDateTime.now());
        if (pet.getPetName().startsWith("Trá»©ng ")) {
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
                .aiMessage("ChÃ o chá»§ nhÃ¢n " + c.getCharacterName() + "! MÃ¬nh vá»«a ná»Ÿ khá»i lá»“ng áº¥p, hÃ£y cÃ¹ng nhau duy trÃ¬ chuá»—i ká»· luáº­t má»—i ngÃ y nhÃ©!")
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
            throw new AppException(ErrorCode.BAD_REQUEST, "Trá»©ng Ä‘ang trong lá»“ng áº¥p chÆ°a thá»ƒ Äƒn! HÃ£y áº¥p ná»Ÿ trá»©ng trÆ°á»›c.");
        }

        Integer usedItemId = null;
        boolean payWithGold = (req == null || req.getConsumableItemId() == null || Boolean.TRUE.equals(req.getUseGold()));

        if (payWithGold) {
            if (c.getGold() < FEED_PET_GOLD_COST) {
                throw new AppException(ErrorCode.INSUFFICIENT_GOLD,
                        "KhÃ´ng Ä‘á»§ VÃ ng Ä‘á»ƒ mua thá»©c Äƒn cho Pet! Cáº§n " + FEED_PET_GOLD_COST + " Gold.");
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
                    .orElseThrow(() -> new AppException(ErrorCode.INSUFFICIENT_ITEM_QTY, "KhÃ´ng cÃ³ váº­t pháº©m nÃ y trong tÃºi Ä‘á»“ Ä‘á»ƒ cho Pet Äƒn!"));
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
                ? "NhoÃ m nhoÃ m! " + pet.getPetName() + " Ä‘Ã£ tá»‰nh giáº¥c sau ká»³ ngá»§ Ä‘Ã´ng vÃ  phá»¥c há»“i toÃ n bá»™ Buff Ä‘á»“ng hÃ nh!"
                : pet.getPetName() + " Äƒn ráº¥t ngon miá»‡ng! (Äá»™ no: " + pet.getHunger() + "/100, ThÃ¢n thiáº¿t: " + pet.getAffection() + "/100).";

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
            throw new AppException(ErrorCode.BAD_REQUEST, "HÃ£y áº¥p ná»Ÿ trá»©ng thÃ nh ThÃº non (Stage 1) trÆ°á»›c khi tiáº¿n hÃ³a!");
        }
        if (pet.getStage() >= NxvConstants.PET_STAGE_ADULT) {
            throw new AppException(ErrorCode.BAD_REQUEST, "ThÃº cÆ°ng nÃ y Ä‘Ã£ á»Ÿ hÃ¬nh thÃ¡i TrÆ°á»Ÿng thÃ nh (Stage 2) tá»‘i Ä‘a rá»“i!");
        }

        int reqDays = (pet.getSpecies().getEvolveDays() != null && pet.getSpecies().getEvolveDays() > 0)
                ? pet.getSpecies().getEvolveDays() : 14;

        long daysSinceHatch = (pet.getHatchedAt() != null)
                ? Duration.between(pet.getHatchedAt(), LocalDateTime.now()).toDays() : 0L;

        boolean eligibleByStreakOrDays = (c.getStreakDays() >= reqDays) || (daysSinceHatch >= reqDays) || (pet.getAffection() >= 80);
        if (!eligibleByStreakOrDays) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "ChÆ°a Ä‘á»§ Ä‘iá»u kiá»‡n tiáº¿n hÃ³a! Cáº§n Ä‘áº¡t chuá»—i Streak " + reqDays + " ngÃ y (Hiá»‡n táº¡i: " + c.getStreakDays()
                            + ") hoáº·c Äá»™ thÃ¢n thiáº¿t >= 80 (Hiá»‡n táº¡i: " + pet.getAffection() + "/100).");
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
                .aiMessage("Ãnh sÃ¡ng tiáº¿n hÃ³a bÃ¹ng ná»•! [" + pet.getPetName() + "] Ä‘Ã£ tiáº¿n hÃ³a lÃªn hÃ¬nh thÃ¡i TrÆ°á»Ÿng thÃ nh nhá» chuá»—i ká»· luáº­t cá»§a báº¡n!")
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
            throw new AppException(ErrorCode.BAD_REQUEST, pet.getPetName() + " Ä‘ang ngá»§ Ä‘Ã´ng vÃ¬ Ä‘Ã³i! HÃ£y cho Pet Äƒn Ä‘á»ƒ Ä‘Ã¡nh thá»©c nhÃ©.");
        }

        pet.setAffection(Math.min(100, pet.getAffection() + 5));
        pet = userPetsRepository.save(pet);

        String[] dialogues = new String[]{
                pet.getPetName() + " dá»¥i Ä‘áº§u vÃ o tay " + c.getCharacterName() + ": \"HÃ´m nay chÃºng ta lÃ m thÃªm 1 Task láº¥y VÃ ng quay há»™p mÃ¹ Ä‘i chá»§ nhÃ¢n!\"",
                pet.getPetName() + " váº«y Ä‘uÃ´i hÃ o há»©ng: \"Chá»‰ sá»‘ ThÃ¢n thiáº¿t Ä‘Ã£ tÄƒng lÃªn " + pet.getAffection() + "/100 rá»“i Ä‘Ã³!\"",
                pet.getPetName() + " nháº¯c nháº¹: \"Äá»«ng quÃªn Ä‘iá»ƒm danh giá»¯ chuá»—i Streak " + c.getStreakDays() + " ngÃ y Ä‘á»ƒ mÃ¬nh sá»›m tiáº¿n hÃ³a nhÃ©!\"",
                pet.getPetName() + " tá»a ra nÄƒng lÆ°á»£ng há»‡ " + pet.getElementType() + ", giÃºp báº¡n nháº­n thÃªm +" + pet.getSpecies().getPassiveGoldBonus().multiply(BigDecimal.valueOf(100)).intValue() + "% VÃ ng má»—i khi hoÃ n thÃ nh Task!"
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
            throw new AppException(ErrorCode.BAD_REQUEST, pet.getPetName() + " Ä‘ang ngá»§ Ä‘Ã´ng vÃ¬ bá»‹ bá» bÃª! HÃ£y cho Pet Äƒn trÆ°á»›c khi trÃ² chuyá»‡n.");
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
        res.put("mood", isLazyState ? "ROASTING_REMIND (Äang khá»‹a/nháº¯c nhá»Ÿ)" : "PRAISING_MOTIVATE (Äang khen ngá»£i/Ä‘á»™ng viÃªn)");
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
                        .aiMessage(pet.getPetName() + " Ä‘Ã£ rÆ¡i vÃ o tráº¡ng thÃ¡i Ngá»§ Ä‘Ã´ng do bá»‹ bá» Ä‘Ã³i hoáº·c chá»§ nhÃ¢n offline quÃ¡ lÃ¢u. Buff thá»¥ Ä‘á»™ng táº¡m thá»i bá»‹ vÃ´ hiá»‡u hÃ³a!")
                        .createdAt(LocalDateTime.now())
                        .build());

                notificationsRepository.save(NxvNotifications.builder()
                        .user(owner)
                        .title("ThÃº cÆ°ng [" + pet.getPetName() + "] Ä‘Ã£ ngá»§ Ä‘Ã´ng!")
                        .content("Do báº¡n bá» bÃª nhiá»‡m vá»¥ hoáº·c chÆ°a cho Äƒn, " + pet.getPetName() + " Ä‘Ã£ ngá»§ Ä‘Ã´ng. HÃ£y vÃ o cho Pet Äƒn Ä‘á»ƒ khÃ´i phá»¥c Buff nhÃ©!")
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

        String aiFeedback = "Nháº­t kÃ½ cá»§a " + c.getCharacterName() + " Ä‘Ã£ Ä‘Æ°á»£c ghi nháº­n! Duy trÃ¬ thÃ³i quen nhÃ¬n láº¡i báº£n thÃ¢n má»—i ngÃ y sáº½ giÃºp báº¡n giá»¯ vá»¯ng chuá»—i Streak " + c.getStreakDays() + " ngÃ y.";
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
                "Chá»§ nhÃ¢n: %s (Level %d, HP: %d/%d, Streak: %d ngÃ y). ThÃº cÆ°ng: %s (Há»‡ %s). Tiáº¿n Ä‘á»™ Task: ÄÃ£ xong %d, Trá»… háº¡n %d, Äang chá» %d (Tá»· lá»‡ hoÃ n thÃ nh: %.1f%%).",
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
                    String sys = (cfg.getSystemPrompt() != null ? cfg.getSystemPrompt() : "Báº¡n lÃ  thÃº cÆ°ng RPG thÃ´ng minh.")
                            + " Dá»¯ liá»‡u hiá»‡n táº¡i: " + contextSummary
                            + (userPrompt != null && !userPrompt.isBlank() ? " CÃ¢u há»i cá»§a chá»§ nhÃ¢n: " + userPrompt : "");

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
            return "[" + pet.getPetName() + " nhÃ­u mÃ y khá»‹a]: \"Chá»§ nhÃ¢n " + c.getCharacterName()
                    + " Æ¡i! Tá»· lá»‡ hoÃ n thÃ nh task Ä‘ang tá»¥t xuá»‘ng " + String.format("%.1f%%", rate)
                    + " (Trá»… háº¡n " + failed + " task, cÃ²n tá»“n " + pending + " task chÆ°a Ä‘á»¥ng tá»›i) kÃ¬a! MÃ¡u nhÃ¢n váº­t chá»‰ cÃ²n "
                    + c.getCurrentHp() + "/" + c.getMaxHp() + " HP thÃ´i, bá»™ Ä‘á»‹nh Ä‘á»ƒ nhÃ¢n váº­t gá»¥c ngÃ£ tá»‘n tiá»n tháº­t quÃ©t VietQR mua VÃ© Há»“i Sinh hay sao? Báº¯t tay xá»­ lÃ½ ngay 1 task Ä‘i nÃ o!\"";
        } else {
            return "[" + pet.getPetName() + " nháº£y lÃªn cá»• vÅ©]: \"Äá»‰nh quÃ¡ chá»§ nhÃ¢n " + c.getCharacterName()
                    + " Æ¡i! Báº¡n Ä‘Ã£ hoÃ n thÃ nh " + completed + " nhiá»‡m vá»¥ vá»›i tá»· lá»‡ " + String.format("%.1f%%", rate)
                    + " vÃ  giá»¯ vá»¯ng chuá»—i Streak " + c.getStreakDays() + " ngÃ y liÃªn tiáº¿p! Cá»© Ä‘Ã  ká»· luáº­t tháº¿ nÃ y thÃ¬ cáº£ mÃ¬nh vÃ  báº¡n Ä‘á»u sá»›m max cáº¥p thÃ´i!\"";
        }
    }

    private UserPetDto toUserPetDto(NxvUserPets p, NxvCharacters c) {
        String stLabel = switch (p.getStage() != null ? p.getStage() : 0) {
            case 2 -> "Adult (TrÆ°á»Ÿng thÃ nh)";
            case 1 -> "Baby (ThÃº non)";
            default -> "Egg (Trá»©ng trong lá»“ng áº¥p)";
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
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
        return u;
    }

    private NxvCharacters getCharacter(Long userId) {
        return charactersRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y nhÃ¢n váº­t!"));
    }

    private NxvUserPets getUserPet(Long userId, Long userPetId) {
        return userPetsRepository.findByUserPetIdAndUser_UserId(userPetId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "KhÃ´ng tÃ¬m tháº¥y ThÃº cÆ°ng cá»§a báº¡n vá»›i ID: " + userPetId));
    }
}