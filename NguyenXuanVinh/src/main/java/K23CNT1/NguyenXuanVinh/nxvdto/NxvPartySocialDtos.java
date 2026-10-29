package K23CNT1.NguyenXuanVinh.nxvdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class NxvPartySocialDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreatePartyRequest {
        @NotBlank(message = "Tên Tổ đội (Party) không được để trống")
        private String partyName;
        private String description;
        private Integer maxMembers;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InviteMemberRequest {
        private Long targetUserId;
        private String targetUsername;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PartyMemberDto {
        private Long partyMemberId;
        private Long userId;
        private String username;
        private String characterName;
        private String avatarUrl;
        private Integer currentLevel;
        private Integer currentHp;
        private Integer maxHp;
        private Boolean isDead;
        private Boolean isShieldActive;
        private Integer role; // 0=Member, 1=Leader, 2=PendingInvite
        private String roleLabel;
        private LocalDateTime joinedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PartyDetailDto {
        private Long partyId;
        private String partyName;
        private String description;
        private Long leaderUserId;
        private String leaderUsername;
        private Integer activeMemberCount;
        private Integer maxMembers;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private List<PartyMemberDto> members;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreatePartySharedTaskRequest {
        @NotBlank(message = "Tiêu đề Task chung của Party không được để trống")
        private String title;
        private String descriptionMarkdown;
        private Integer difficulty; // 1=Easy, 2=Normal, 3=Hard
        private Integer rewardExp;
        private Integer rewardGold;
        private Integer penaltyHp;
        private LocalDateTime deadlineAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreatePartyBossQuestRequest {
        @NotBlank(message = "Tên Boss / Dự án chung của Party không được để trống")
        private String title;
        private Integer rewardExp;
        private Integer rewardGold;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendPartyMessageRequest {
        @NotBlank(message = "Nội dung tin nhắn không được để trống")
        private String content;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PartyMessageDto {
        private Long messageId;
        private Long partyId;
        private Long senderUserId;
        private String senderUsername;
        private String senderCharacterName;
        private String senderAvatarUrl;
        private String content;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FriendRequestDto {
        private Long targetUserId;
        private String targetUsername;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FriendshipDto {
        private Long friendshipId;
        private Long friendUserId;
        private String friendUsername;
        private String friendCharacterName;
        private String friendAvatarUrl;
        private Integer friendLevel;
        private Integer status; // 0=Pending, 1=Accepted, 2=Declined
        private String statusLabel;
        private Boolean isIncomingRequest;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LeaderboardEntryDto {
        private Integer rank;
        private Long userId;
        private String characterName;
        private Integer currentLevel;
        private Long currentExp;
        private Long gold;
        private Integer totalTasksDone;
        private Long periodScore; // Diem cay Vang hoac Task trong Tuan/Thang
    }
}