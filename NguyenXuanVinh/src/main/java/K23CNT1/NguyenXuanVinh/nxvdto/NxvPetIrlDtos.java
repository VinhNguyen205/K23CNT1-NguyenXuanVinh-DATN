package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NxvPetIrlDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AdoptEggRequest {
        private Integer speciesId; // Null = random loai thu cung trong NxvPetSpecies
        private String petName;
        private String elementType;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RenameAndElementRequest {
        @NotBlank(message = "TÃªn thÃº cÆ°ng khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String petName;
        private String elementType; // Hoa, Loi, Thuy, Moc, Tu nhien
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FeedPetRequest {
        @Builder.Default
        private Boolean useGold = true; // true = cho an bang Vang (25 Gold), false = cho an bang Item tieu hao
        private Integer consumableItemId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AIChatPetRequest {
        private String userPrompt; // Tuy chon: Nguoi choi hoi chuyen Pet hoac de trong de Pet tu phan tich Task
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserPetDto {
        private Long userPetId;
        private Long userId;
        private Integer speciesId;
        private String speciesName;
        private String petName;
        private String elementType;
        private Integer stage; // 0=Egg, 1=Baby, 2=Adult
        private String stageLabel;
        private String currentImageUrl; // Anh 2D tinh tren Dashboard
        private Integer hunger; // 0..100
        private Integer affection; // 0..100
        private Boolean isActive;
        private Boolean isHibernating;
        private Integer evolveDaysRequired;
        private Boolean canEvolveNow;
        private BigDecimal passiveGoldBonus;
        private BigDecimal passiveDropRateBonus;
        private Boolean isBuffCurrentlyActive;
        private LocalDateTime hatchedAt;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PetInteractionLogDto {
        private Long logId;
        private Long userPetId;
        private String petName;
        private String interactionType; // FEED, PET, AI_REMIND, AI_PRAISE, EVOLVE, HIBERNATE
        private Integer usedItemId;
        private String aiMessage;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PetDexEntryDto {
        private Integer speciesId;
        private String speciesName;
        private String defaultElementType;
        private String eggImageUrl;
        private String babyImageUrl;
        private String adultImageUrl;
        private Integer evolveDays;
        private BigDecimal passiveGoldBonus;
        private BigDecimal passiveDropRateBonus;
        private Boolean isUnlocked;
        private LocalDateTime firstObtainedAt;
    }

    // --- IRL JOURNAL & FOCUS SESSION DTOS ---
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateJournalRequest {
        @NotBlank(message = "Ná»™i dung nháº­t kÃ½ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        private String content;
        private String moodTag; // HAPPY, TIRED, FOCUSED, STRESSED, DETERMINED
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class JournalDto {
        private Long journalId;
        private Long userId;
        private String content;
        private String moodTag;
        private String aiResponse;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CompleteFocusRequest {
        @NotNull(message = "Thá»i lÆ°á»£ng táº­p trung (phÃºt) khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
        @Min(value = 10, message = "Thá»i lÆ°á»£ng táº­p trung tá»‘i thiá»ƒu lÃ  10 phÃºt Ä‘á»ƒ nháº­n thÆ°á»Ÿng")
        private Integer durationMinutes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FocusLogDto {
        private Long focusLogId;
        private Integer durationMinutes;
        private Integer rewardExp;
        private Integer rewardGold;
        private LocalDateTime createdAt;
    }
}