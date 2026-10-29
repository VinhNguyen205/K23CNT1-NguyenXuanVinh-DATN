package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.NxvPartySocialDtos.*;
import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvenums.NxvConstants;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NxvPartySocialService {

    private final UsersRepository usersRepository;
    private final NxvCharactersRepository charactersRepository;
    private final NxvPartiesRepository partiesRepository;
    private final NxvPartyMembersRepository partyMembersRepository;
    private final NxvPartyMessagesRepository partyMessagesRepository;
    private final NxvFriendshipsRepository friendshipsRepository;
    private final NxvTasksRepository tasksRepository;
    private final NxvTaskHistoryRepository taskHistoryRepository;
    private final NxvEpicQuestsRepository epicQuestsRepository;
    private final NxvVwUserTotalBuffsRepository totalBuffsRepository;
    private final NxvVwLevelLeaderboardRepository levelLeaderboardRepository;
    private final NxvVwGoldLeaderboardRepository goldLeaderboardRepository;
    private final NxvVwTaskLeaderboardRepository taskLeaderboardRepository;
    private final NxvStatLogsRepository statLogsRepository;
    private final NxvNotificationsRepository notificationsRepository;
    private final NxvCharacterService characterService;

    // =========================================================================
    // PHAN 1: QUAN LY TO DOI (PARTY) (#91 -> #94)
    // =========================================================================

    // Chuc nang #91: Khoi tao To doi (Party)
    @Transactional
    public PartyDetailDto createParty(Long userId, CreatePartyRequest req) {
        Users leader = getUserAlive(userId);
        int maxMem = (req.getMaxMembers() != null && req.getMaxMembers() >= 2 && req.getMaxMembers() <= 20)
                ? req.getMaxMembers() : 5;

        NxvParties party = NxvParties.builder()
                .partyName(req.getPartyName().trim())
                .description(req.getDescription())
                .leaderUser(leader)
                .maxMembers(maxMem)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        party = partiesRepository.save(party);

        partyMembersRepository.save(NxvPartyMembers.builder()
                .party(party)
                .user(leader)
                .role(1) // 1 = Leader
                .joinedAt(LocalDateTime.now())
                .build());

        return getPartyDetail(party.getPartyId());
    }

    @Transactional(readOnly = true)
    public List<PartyDetailDto> getMyParties(Long userId) {
        return partyMembersRepository.findByUser_UserIdOrderByJoinedAtDesc(userId).stream()
                .map(pm -> getPartyDetail(pm.getParty().getPartyId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartyDetailDto getPartyDetail(Long partyId) {
        NxvParties party = getActiveParty(partyId);
        List<NxvPartyMembers> rawMembers = partyMembersRepository.findByParty_PartyIdOrderByRoleAscJoinedAtAsc(partyId);

        List<PartyMemberDto> memberDtos = rawMembers.stream().map(pm -> {
            Users u = pm.getUser();
            NxvCharacters c = charactersRepository.findByUser_UserId(u.getUserId()).orElse(null);
            String rLabel = switch (pm.getRole() != null ? pm.getRole() : 0) {
                case 1 -> "Leader (Đội trưởng)";
                case 2 -> "PendingInvite (Đang chờ phản hồi)";
                default -> "Member (Thành viên)";
            };
            return PartyMemberDto.builder()
                    .partyMemberId(pm.getPartyMemberId())
                    .userId(u.getUserId())
                    .username(u.getUsername())
                    .characterName(c != null ? c.getCharacterName() : u.getUsername())
                    .avatarUrl(c != null ? c.getAvatarUrl() : u.getDefaultAvatarUrl())
                    .currentLevel(c != null ? c.getCurrentLevel() : 1)
                    .currentHp(c != null ? c.getCurrentHp() : 100)
                    .maxHp(c != null ? c.getMaxHp() : 100)
                    .isDead(u.getIsDead())
                    .isShieldActive(c != null ? c.getIsShieldActive() : false)
                    .role(pm.getRole())
                    .roleLabel(rLabel)
                    .joinedAt(pm.getJoinedAt())
                    .build();
        }).collect(Collectors.toList());

        int activeCount = (int) rawMembers.stream()
                .filter(m -> m.getRole() != null && (m.getRole() == 0 || m.getRole() == 1))
                .count();

        return PartyDetailDto.builder()
                .partyId(party.getPartyId())
                .partyName(party.getPartyName())
                .description(party.getDescription())
                .leaderUserId(party.getLeaderUser().getUserId())
                .leaderUsername(party.getLeaderUser().getUsername())
                .activeMemberCount(activeCount)
                .maxMembers(party.getMaxMembers())
                .isActive(party.getIsActive())
                .createdAt(party.getCreatedAt())
                .members(memberDtos)
                .build();
    }

    // Chuc nang #92: Moi nguoi choi khac vao Party (Qua ID hoac username)
    @Transactional
    public PartyDetailDto invitePlayerToParty(Long inviterUserId, Long partyId, InviteMemberRequest req) {
        Users inviter = getUserAlive(inviterUserId);
        NxvParties party = getActiveParty(partyId);
        assertUserIsActiveMember(partyId, inviterUserId);

        long currentActiveCount = partyMembersRepository.countByParty_PartyIdAndRoleIn(partyId, List.of(0, 1));
        if (currentActiveCount >= party.getMaxMembers()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Party đã đạt số lượng thành viên tối đa (" + party.getMaxMembers() + " người)!");
        }

        Users targetUser = resolveTargetUser(req.getTargetUserId(), req.getTargetUsername());
        if (partyMembersRepository.findByParty_PartyIdAndUser_UserId(partyId, targetUser.getUserId()).isPresent()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Người chơi này đã có trong Party hoặc đang có lời mời chờ duyệt!");
        }

        partyMembersRepository.save(NxvPartyMembers.builder()
                .party(party)
                .user(targetUser)
                .role(2) // 2 = PendingInvite
                .joinedAt(LocalDateTime.now())
                .build());

        notificationsRepository.save(NxvNotifications.builder()
                .user(targetUser)
                .title("Lời mời gia nhập Tổ đội: " + party.getPartyName())
                .content(inviter.getUsername() + " đã mời bạn gia nhập Party [" + party.getPartyName() + "] (PartyID: " + partyId + ").")
                .notificationType("PARTY")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return getPartyDetail(partyId);
    }

    // Chuc nang #93: Chap nhan / Tu choi loi moi vao Party
    @Transactional
    public Map<String, Object> respondToPartyInvite(Long userId, Long partyId, boolean accept) {
        getUserAlive(userId);
        NxvParties party = getActiveParty(partyId);
        NxvPartyMembers membership = partyMembersRepository.findByParty_PartyIdAndUser_UserId(partyId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bạn không có lời mời nào từ Party này!"));

        if (membership.getRole() != 2) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Bạn đã là thành viên chính thức của Party này rồi!");
        }

        if (accept) {
            long currentActiveCount = partyMembersRepository.countByParty_PartyIdAndRoleIn(partyId, List.of(0, 1));
            if (currentActiveCount >= party.getMaxMembers()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Party vừa đầy (" + party.getMaxMembers() + " thành viên), không thể tham gia!");
            }
            membership.setRole(0); // 0 = Member chinh thuc
            membership.setJoinedAt(LocalDateTime.now());
            partyMembersRepository.save(membership);
            return Map.of("partyId", partyId, "status", "ACCEPTED", "party", getPartyDetail(partyId));
        } else {
            partyMembersRepository.delete(membership);
            return Map.of("partyId", partyId, "status", "DECLINED");
        }
    }

    // Chuc nang #94A: Doi truong Kich thanh vien khoi Party
    @Transactional
    public PartyDetailDto kickMember(Long leaderUserId, Long partyId, Long targetUserId) {
        NxvParties party = getActiveParty(partyId);
        if (!Objects.equals(party.getLeaderUser().getUserId(), leaderUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Chỉ Đội trưởng (Leader) mới có quyền kích thành viên khỏi Party!");
        }
        if (Objects.equals(leaderUserId, targetUserId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể tự kích chính mình! Hãy dùng chức năng Rời Party.");
        }

        NxvPartyMembers targetMem = partyMembersRepository.findByParty_PartyIdAndUser_UserId(partyId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Người chơi này không thuộc Party!"));
        partyMembersRepository.delete(targetMem);
        return getPartyDetail(partyId);
    }

    // Chuc nang #94B: Roi khoi Party (Neu Leader roi thi nhường Leader hoac giai tan)
    @Transactional
    public Map<String, Object> leaveParty(Long userId, Long partyId) {
        NxvParties party = getActiveParty(partyId);
        NxvPartyMembers myMem = partyMembersRepository.findByParty_PartyIdAndUser_UserId(partyId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Bạn không thuộc Party này!"));

        partyMembersRepository.delete(myMem);

        List<NxvPartyMembers> remainingActive = partyMembersRepository.findByParty_PartyIdOrderByRoleAscJoinedAtAsc(partyId)
                .stream()
                .filter(m -> m.getRole() != null && (m.getRole() == 0 || m.getRole() == 1) && !Objects.equals(m.getUser().getUserId(), userId))
                .collect(Collectors.toList());

        if (remainingActive.isEmpty()) {
            party.setIsActive(false);
            partiesRepository.save(party);
            return Map.of("partyId", partyId, "message", "Bạn đã rời Party. Party không còn thành viên nên đã được giải tán!");
        } else if (Objects.equals(party.getLeaderUser().getUserId(), userId)) {
            NxvPartyMembers nextLeader = remainingActive.get(0);
            nextLeader.setRole(1);
            partyMembersRepository.save(nextLeader);
            party.setLeaderUser(nextLeader.getUser());
            partiesRepository.save(party);
        }

        return Map.of("partyId", partyId, "message", "Đã rời khỏi Party thành công!");
    }

    // =========================================================================
    // PHAN 2: SHARE TASK, SAT THUONG CHUNG CA DOI & DANH BOSS (#95, #96, #97)
    // =========================================================================

    // Chuc nang #95: Share Task - Tao va giao Task chung cho toan bo thanh vien trong Party
    @Transactional
    public Map<String, Object> createPartySharedTask(Long userId, Long partyId, CreatePartySharedTaskRequest req) {
        Users creator = getUserAlive(userId);
        NxvParties party = getActiveParty(partyId);
        assertUserIsActiveMember(partyId, userId);

        int diff = (req.getDifficulty() != null && req.getDifficulty() >= 1 && req.getDifficulty() <= 3) ? req.getDifficulty() : 2;
        int exp = req.getRewardExp() != null ? req.getRewardExp() : (diff * 30);
        int gold = req.getRewardGold() != null ? req.getRewardGold() : (diff * 20);
        int penaltyHp = req.getPenaltyHp() != null ? req.getPenaltyHp() : (diff * 15);
        LocalDateTime deadline = req.getDeadlineAt() != null ? req.getDeadlineAt() : LocalDateTime.now().plusHours(24);

        // 1. Tao Task Goc cua Party (Master Shared Task)
        NxvTasks rootTask = NxvTasks.builder()
                .user(creator)
                .title("[PARTY: " + party.getPartyName() + "] " + req.getTitle().trim())
                .descriptionMarkdown(req.getDescriptionMarkdown())
                .colorLabel("#EF4444")
                .difficulty(diff)
                .rewardExp(exp)
                .rewardGold(gold)
                .rewardMp(10)
                .penaltyHp(penaltyHp)
                .status(NxvConstants.TASK_PENDING)
                .sortOrder(0)
                .deadlineAt(deadline)
                .isFrozen(false)
                .isShielded(false)
                .undoAvailable(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        rootTask = tasksRepository.save(rootTask);
        rootTask.setPartyTask(rootTask);
        rootTask = tasksRepository.save(rootTask);

        // 2. Nhan ban Task cho cac thanh vien chinh thuc khac trong Party
        List<NxvPartyMembers> activeMembers = partyMembersRepository.findByParty_PartyIdOrderByRoleAscJoinedAtAsc(partyId)
                .stream()
                .filter(m -> m.getRole() != null && (m.getRole() == 0 || m.getRole() == 1))
                .collect(Collectors.toList());

        List<Long> spawnedTaskIds = new ArrayList<>();
        spawnedTaskIds.add(rootTask.getTaskId());

        for (NxvPartyMembers mem : activeMembers) {
            Users memUser = mem.getUser();
            if (!Objects.equals(memUser.getUserId(), creator.getUserId())) {
                NxvTasks childTask = NxvTasks.builder()
                        .user(memUser)
                        .partyTask(rootTask)
                        .title("[PARTY: " + party.getPartyName() + "] " + req.getTitle().trim())
                        .descriptionMarkdown(req.getDescriptionMarkdown())
                        .colorLabel("#EF4444")
                        .difficulty(diff)
                        .rewardExp(exp)
                        .rewardGold(gold)
                        .rewardMp(10)
                        .penaltyHp(penaltyHp)
                        .status(NxvConstants.TASK_PENDING)
                        .sortOrder(0)
                        .deadlineAt(deadline)
                        .isFrozen(false)
                        .isShielded(false)
                        .undoAvailable(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                childTask = tasksRepository.save(childTask);
                spawnedTaskIds.add(childTask.getTaskId());
            }

            notificationsRepository.save(NxvNotifications.builder()
                    .user(memUser)
                    .title("Nhiệm vụ Tổ đội mới: " + party.getPartyName())
                    .content("Party vừa nhận nhiệm vụ chung: \"" + req.getTitle().trim() + "\". Cảnh báo: Nếu 1 người trễ hạn, cả đội sẽ cùng bị trừ máu!")
                    .notificationType("PARTY")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("partyId", partyId);
        res.put("rootPartyTaskId", rootTask.getTaskId());
        res.put("assignedMembersCount", activeMembers.size());
        res.put("spawnedTaskIds", spawnedTaskIds);
        return res;
    }

    // Chuc nang #96: Thuat toan Party - Tinh sat thuong chung (1 nguoi fail task chia se -> Tru HP ca doi)
    @Transactional
    public Map<String, Object> triggerSharedTaskTeamPenalty(Long partyId, Long failedTaskId) {
        NxvParties party = getActiveParty(partyId);
        NxvTasks failedTask = tasksRepository.findById(failedTaskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND, "Không tìm thấy Task ID: " + failedTaskId));

        failedTask.setStatus(NxvConstants.TASK_FAILED);
        failedTask.setUpdatedAt(LocalDateTime.now());
        tasksRepository.save(failedTask);

        taskHistoryRepository.save(NxvTaskHistory.builder()
                .task(failedTask)
                .user(failedTask.getUser())
                .actionType("PARTY_SHARED_FAIL")
                .actionAt(LocalDateTime.now())
                .build());

        int basePenalty = (failedTask.getPenaltyHp() != null && failedTask.getPenaltyHp() > 0) ? failedTask.getPenaltyHp() : 20;

        List<NxvPartyMembers> activeMembers = partyMembersRepository.findByParty_PartyIdOrderByRoleAscJoinedAtAsc(partyId)
                .stream()
                .filter(m -> m.getRole() != null && (m.getRole() == 0 || m.getRole() == 1))
                .collect(Collectors.toList());

        List<Map<String, Object>> memberDamageResults = new ArrayList<>();

        for (NxvPartyMembers mem : activeMembers) {
            Users memUser = mem.getUser();
            NxvCharacters c = charactersRepository.findByUser_UserId(memUser.getUserId()).orElse(null);
            if (c == null || Boolean.TRUE.equals(memUser.getIsDead())) continue;

            Map<String, Object> dmgReport = new LinkedHashMap<>();
            dmgReport.put("userId", memUser.getUserId());
            dmgReport.put("characterName", c.getCharacterName());

            // Kiem tra Khien chan cap nhan vat (IsShieldActive - v2.2)
            if (Boolean.TRUE.equals(c.getIsShieldActive())) {
                c.setIsShieldActive(false);
                c.setUpdatedAt(LocalDateTime.now());
                charactersRepository.save(c);

                statLogsRepository.save(NxvStatLogs.builder()
                        .user(memUser)
                        .logType("HP")
                        .delta(0L)
                        .sourceType("PartyShieldBlocked")
                        .sourceId(failedTaskId)
                        .createdAt(LocalDateTime.now())
                        .build());

                dmgReport.put("damageTaken", 0);
                dmgReport.put("shieldConsumed", true);
                dmgReport.put("remainingHp", c.getCurrentHp());
                dmgReport.put("isDead", false);
            } else {
                NxvVwUserTotalBuffs buffs = totalBuffsRepository.findById(memUser.getUserId()).orElse(null);
                BigDecimal dmgReduce = (buffs != null && buffs.getTotalDmgReduce() != null)
                        ? buffs.getTotalDmgReduce() : BigDecimal.ZERO;

                int reduced = (int) Math.floor(basePenalty * dmgReduce.doubleValue());
                int actualPenalty = Math.max(1, basePenalty - reduced);
                int newHp = Math.max(0, c.getCurrentHp() - actualPenalty);

                c.setCurrentHp(newHp);
                c.setUpdatedAt(LocalDateTime.now());
                charactersRepository.save(c);

                if (newHp == 0) {
                    memUser.setIsDead(true);
                    memUser.setUpdatedAt(LocalDateTime.now());
                    usersRepository.save(memUser);
                }

                statLogsRepository.save(NxvStatLogs.builder()
                        .user(memUser)
                        .logType("HP")
                        .delta((long) -actualPenalty)
                        .sourceType("PartySharedFail")
                        .sourceId(failedTaskId)
                        .createdAt(LocalDateTime.now())
                        .build());

                dmgReport.put("damageTaken", actualPenalty);
                dmgReport.put("shieldConsumed", false);
                dmgReport.put("remainingHp", newHp);
                dmgReport.put("isDead", newHp == 0);
            }

            notificationsRepository.save(NxvNotifications.builder()
                    .user(memUser)
                    .title("Phạt sát thương liên đới Tổ đội [" + party.getPartyName() + "]")
                    .content("Thành viên " + failedTask.getUser().getUsername() + " đã trễ hạn Task chung \"" + failedTask.getTitle() + "\". Cả đội bị phạt sát thương HP!")
                    .notificationType("PARTY")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build());

            memberDamageResults.add(dmgReport);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("partyId", partyId);
        res.put("partyName", party.getPartyName());
        res.put("failedTaskId", failedTaskId);
        res.put("failedByUser", failedTask.getUser().getUsername());
        res.put("teamDamageReports", memberDamageResults);
        return res;
    }

    // Chuc nang #97: Tao Boss (Epic Quest chung) & Chia phan thuong khi ca doi hoan thanh Boss
    @Transactional
    public NxvEpicQuests createPartyBossQuest(Long userId, Long partyId, CreatePartyBossQuestRequest req) {
        Users creator = getUserAlive(userId);
        NxvParties party = getActiveParty(partyId);
        assertUserIsActiveMember(partyId, userId);

        return epicQuestsRepository.save(NxvEpicQuests.builder()
                .user(creator)
                .party(party)
                .title(req.getTitle().trim())
                .isGpaQuest(false)
                .rewardExp(req.getRewardExp() != null ? req.getRewardExp() : 600)
                .rewardGold(req.getRewardGold() != null ? req.getRewardGold() : 400)
                .status(0)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public Map<String, Object> completePartyBossAndDistributeRewards(Long userId, Long partyId, Long epicQuestId) {
        getUserAlive(userId);
        NxvParties party = getActiveParty(partyId);
        assertUserIsActiveMember(partyId, userId);

        NxvEpicQuests bossQuest = epicQuestsRepository.findById(epicQuestId)
                .filter(eq -> eq.getParty() != null && Objects.equals(eq.getParty().getPartyId(), partyId))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Boss / Epic Quest chung của Party này!"));

        if (bossQuest.getStatus() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Boss / Epic Quest chung này đã được tiêu diệt và chia thưởng rồi!");
        }

        bossQuest.setStatus(1);
        epicQuestsRepository.save(bossQuest);

        List<NxvPartyMembers> activeMembers = partyMembersRepository.findByParty_PartyIdOrderByRoleAscJoinedAtAsc(partyId)
                .stream()
                .filter(m -> m.getRole() != null && (m.getRole() == 0 || m.getRole() == 1))
                .collect(Collectors.toList());

        long baseExp = bossQuest.getRewardExp() != null ? bossQuest.getRewardExp() : 600L;
        long baseGold = bossQuest.getRewardGold() != null ? bossQuest.getRewardGold() : 400L;

        List<Map<String, Object>> rewardReports = new ArrayList<>();
        for (NxvPartyMembers mem : activeMembers) {
            Users memUser = mem.getUser();
            if (Boolean.TRUE.equals(memUser.getIsDead())) continue;

            NxvCharacters c = charactersRepository.findByUser_UserId(memUser.getUserId()).orElse(null);
            if (c == null) continue;

            c.setGold(c.getGold() + baseGold);
            statLogsRepository.save(NxvStatLogs.builder()
                    .user(memUser)
                    .logType("Gold")
                    .delta(baseGold)
                    .sourceType("PartyBossReward")
                    .sourceId(epicQuestId)
                    .createdAt(LocalDateTime.now())
                    .build());

            int lvGained = characterService.addExpAndCheckLevelUp(c, baseExp, "PartyBossReward", epicQuestId);

            Map<String, Object> r = new LinkedHashMap<>();
            r.put("userId", memUser.getUserId());
            r.put("characterName", c.getCharacterName());
            r.put("expGained", baseExp);
            r.put("goldGained", baseGold);
            r.put("levelsGained", lvGained);
            rewardReports.add(r);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("partyId", partyId);
        res.put("partyName", party.getPartyName());
        res.put("epicQuestId", epicQuestId);
        res.put("bossTitle", bossQuest.getTitle());
        res.put("memberRewards", rewardReports);
        return res;
    }

    // =========================================================================
    // PHAN 3: CHAT NOI BO NHOM TRONG PARTY (#98)
    // =========================================================================

    @Transactional
    public PartyMessageDto sendPartyMessage(Long userId, Long partyId, SendPartyMessageRequest req) {
        Users sender = getUserAlive(userId);
        NxvParties party = getActiveParty(partyId);
        assertUserIsActiveMember(partyId, userId);

        NxvPartyMessages msg = partyMessagesRepository.save(NxvPartyMessages.builder()
                .party(party)
                .senderUser(sender)
                .content(req.getContent().trim())
                .createdAt(LocalDateTime.now())
                .build());

        NxvCharacters c = charactersRepository.findByUser_UserId(userId).orElse(null);
        return PartyMessageDto.builder()
                .messageId(msg.getMessageId())
                .partyId(partyId)
                .senderUserId(sender.getUserId())
                .senderUsername(sender.getUsername())
                .senderCharacterName(c != null ? c.getCharacterName() : sender.getUsername())
                .senderAvatarUrl(c != null ? c.getAvatarUrl() : sender.getDefaultAvatarUrl())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PartyMessageDto> getPartyMessages(Long userId, Long partyId) {
        assertUserIsActiveMember(partyId, userId);
        List<NxvPartyMessages> list = partyMessagesRepository.findTop100ByParty_PartyIdOrderByCreatedAtDesc(partyId);
        return list.stream().map(msg -> {
            Users u = msg.getSenderUser();
            NxvCharacters c = charactersRepository.findByUser_UserId(u.getUserId()).orElse(null);
            return PartyMessageDto.builder()
                    .messageId(msg.getMessageId())
                    .partyId(partyId)
                    .senderUserId(u.getUserId())
                    .senderUsername(u.getUsername())
                    .senderCharacterName(c != null ? c.getCharacterName() : u.getUsername())
                    .senderAvatarUrl(c != null ? c.getAvatarUrl() : u.getDefaultAvatarUrl())
                    .content(msg.getContent())
                    .createdAt(msg.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    // =========================================================================
    // PHAN 4: BANG XEP HANG SERVER & TUAN/THANG (#99, #100)
    // =========================================================================

    // Chuc nang #99: Bang xep hang Cap do nhan vat cao nhat toan server (NxvVw_LevelLeaderboard)
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLevelLeaderboard() {
        List<NxvVwLevelLeaderboard> rows = levelLeaderboardRepository.findAll();
        List<LeaderboardEntryDto> result = new ArrayList<>();
        int rank = 1;
        for (NxvVwLevelLeaderboard r : rows) {
            result.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(r.getUserId())
                    .characterName(r.getCharacterName())
                    .currentLevel(r.getCurrentLevel())
                    .currentExp(r.getCurrentExp())
                    .gold(r.getGold())
                    .build());
        }
        return result;
    }

    // Chuc nang #100A: Bang xep hang nguoi cay nhieu Vang nhat (All-time View hoac theo Tuan/Thang)
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getGoldLeaderboard(String period) {
        if ("WEEK".equalsIgnoreCase(period) || "MONTH".equalsIgnoreCase(period)) {
            LocalDateTime since = "WEEK".equalsIgnoreCase(period)
                    ? LocalDateTime.now().minusDays(7) : LocalDateTime.now().minusDays(30);

            Map<Long, Long> goldEarnedByUser = statLogsRepository.findAll().stream()
                    .filter(l -> "Gold".equalsIgnoreCase(l.getLogType()) && l.getDelta() != null && l.getDelta() > 0)
                    .filter(l -> l.getCreatedAt() != null && l.getCreatedAt().isAfter(since))
                    .collect(Collectors.groupingBy(
                            l -> l.getUser().getUserId(),
                            Collectors.summingLong(NxvStatLogs::getDelta)
                    ));

            List<LeaderboardEntryDto> list = new ArrayList<>();
            goldEarnedByUser.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(100)
                    .forEachOrdered(e -> {
                        NxvCharacters c = charactersRepository.findByUser_UserId(e.getKey()).orElse(null);
                        if (c != null) {
                            list.add(LeaderboardEntryDto.builder()
                                    .rank(list.size() + 1)
                                    .userId(e.getKey())
                                    .characterName(c.getCharacterName())
                                    .currentLevel(c.getCurrentLevel())
                                    .gold(c.getGold())
                                    .periodScore(e.getValue())
                                    .build());
                        }
                    });
            return list;
        }

        List<NxvVwGoldLeaderboard> rows = goldLeaderboardRepository.findAll();
        List<LeaderboardEntryDto> result = new ArrayList<>();
        int rank = 1;
        for (NxvVwGoldLeaderboard r : rows) {
            result.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(r.getUserId())
                    .characterName(r.getCharacterName())
                    .currentLevel(r.getCurrentLevel())
                    .gold(r.getGold())
                    .periodScore(r.getGold())
                    .build());
        }
        return result;
    }

    // Chuc nang #100B: Bang xep hang nguoi hoan thanh nhieu Task nhat (All-time View hoac theo Tuan/Thang)
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getTaskLeaderboard(String period) {
        if ("WEEK".equalsIgnoreCase(period) || "MONTH".equalsIgnoreCase(period)) {
            LocalDateTime since = "WEEK".equalsIgnoreCase(period)
                    ? LocalDateTime.now().minusDays(7) : LocalDateTime.now().minusDays(30);

            Map<Long, Long> tasksByUser = taskHistoryRepository.findAll().stream()
                    .filter(h -> "COMPLETE".equalsIgnoreCase(h.getActionType()))
                    .filter(h -> h.getActionAt() != null && h.getActionAt().isAfter(since))
                    .collect(Collectors.groupingBy(
                            h -> h.getUser().getUserId(),
                            Collectors.counting()
                    ));

            List<LeaderboardEntryDto> list = new ArrayList<>();
            tasksByUser.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(100)
                    .forEachOrdered(e -> {
                        NxvCharacters c = charactersRepository.findByUser_UserId(e.getKey()).orElse(null);
                        if (c != null) {
                            list.add(LeaderboardEntryDto.builder()
                                    .rank(list.size() + 1)
                                    .userId(e.getKey())
                                    .characterName(c.getCharacterName())
                                    .currentLevel(c.getCurrentLevel())
                                    .totalTasksDone(c.getTotalTasksDone())
                                    .periodScore(e.getValue())
                                    .build());
                        }
                    });
            return list;
        }

        List<NxvVwTaskLeaderboard> rows = taskLeaderboardRepository.findAll();
        List<LeaderboardEntryDto> result = new ArrayList<>();
        int rank = 1;
        for (NxvVwTaskLeaderboard r : rows) {
            result.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(r.getUserId())
                    .characterName(r.getCharacterName())
                    .totalTasksDone(r.getTotalTasksDone())
                    .periodScore(r.getTotalTasksDone() != null ? r.getTotalTasksDone().longValue() : 0L)
                    .build());
        }
        return result;
    }

    // =========================================================================
    // PHAN 5: HE THONG KET BAN (FRIENDSHIPS) (#102)
    // =========================================================================

    @Transactional
    public FriendshipDto sendFriendRequest(Long requesterId, FriendRequestDto req) {
        Users requester = getUserAlive(requesterId);
        Users target = resolveTargetUser(req.getTargetUserId(), req.getTargetUsername());

        if (Objects.equals(requesterId, target.getUserId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Không thể tự gửi lời mời kết bạn cho chính mình!");
        }

        Optional<NxvFriendships> existing = friendshipsRepository
                .findByRequester_UserIdAndAddressee_UserId(requesterId, target.getUserId())
                .or(() -> friendshipsRepository.findByRequester_UserIdAndAddressee_UserId(target.getUserId(), requesterId));

        if (existing.isPresent()) {
            NxvFriendships f = existing.get();
            if (f.getStatus() == 1) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Hai bạn đã là bạn bè của nhau rồi!");
            } else if (f.getStatus() == 0) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Đã có lời mời kết bạn đang chờ phản hồi giữa hai bạn!");
            }
            f.setRequester(requester);
            f.setAddressee(target);
            f.setStatus(0);
            f.setCreatedAt(LocalDateTime.now());
            return toFriendshipDto(friendshipsRepository.save(f), requesterId);
        }

        NxvFriendships created = friendshipsRepository.save(NxvFriendships.builder()
                .requester(requester)
                .addressee(target)
                .status(0)
                .createdAt(LocalDateTime.now())
                .build());

        notificationsRepository.save(NxvNotifications.builder()
                .user(target)
                .title("Lời mời kết bạn mới")
                .content(requester.getUsername() + " đã gửi cho bạn lời mời kết bạn!")
                .notificationType("SYSTEM")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return toFriendshipDto(created, requesterId);
    }

    @Transactional
    public FriendshipDto respondToFriendRequest(Long userId, Long friendshipId, boolean accept) {
        getUserAlive(userId);
        NxvFriendships f = friendshipsRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy lời mời kết bạn ID: " + friendshipId));

        if (!Objects.equals(f.getAddressee().getUserId(), userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không phải là người nhận lời mời kết bạn này!");
        }
        if (f.getStatus() != 0) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Lời mời kết bạn này đã được xử lý trước đó!");
        }

        f.setStatus(accept ? 1 : 2);
        return toFriendshipDto(friendshipsRepository.save(f), userId);
    }

    @Transactional(readOnly = true)
    public List<FriendshipDto> getMyFriendships(Long userId, Integer statusFilter) {
        return friendshipsRepository.findByRequester_UserIdOrAddressee_UserIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .filter(f -> statusFilter == null || Objects.equals(f.getStatus(), statusFilter))
                .map(f -> toFriendshipDto(f, userId))
                .collect(Collectors.toList());
    }

    // --- Helpers ---
    private FriendshipDto toFriendshipDto(NxvFriendships f, Long currentUserId) {
        boolean incoming = Objects.equals(f.getAddressee().getUserId(), currentUserId);
        Users friendUser = incoming ? f.getRequester() : f.getAddressee();
        NxvCharacters c = charactersRepository.findByUser_UserId(friendUser.getUserId()).orElse(null);

        String stLabel = switch (f.getStatus() != null ? f.getStatus() : 0) {
            case 1 -> "ACCEPTED (Đã kết bạn)";
            case 2 -> "DECLINED (Đã từ chối)";
            default -> "PENDING (Đang chờ xác nhận)";
        };

        return FriendshipDto.builder()
                .friendshipId(f.getFriendshipId())
                .friendUserId(friendUser.getUserId())
                .friendUsername(friendUser.getUsername())
                .friendCharacterName(c != null ? c.getCharacterName() : friendUser.getUsername())
                .friendAvatarUrl(c != null ? c.getAvatarUrl() : friendUser.getDefaultAvatarUrl())
                .friendLevel(c != null ? c.getCurrentLevel() : 1)
                .status(f.getStatus())
                .statusLabel(stLabel)
                .isIncomingRequest(incoming)
                .createdAt(f.getCreatedAt())
                .build();
    }

    private Users resolveTargetUser(Long targetUserId, String targetUsername) {
        if (targetUserId != null) {
            return usersRepository.findById(targetUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người chơi với ID: " + targetUserId));
        }
        if (targetUsername != null && !targetUsername.isBlank()) {
            return usersRepository.findByUsername(targetUsername.trim())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người chơi với Username: " + targetUsername));
        }
        throw new AppException(ErrorCode.BAD_REQUEST, "Vui lòng truyền targetUserId hoặc targetUsername!");
    }

    private void assertUserIsActiveMember(Long partyId, Long userId) {
        NxvPartyMembers pm = partyMembersRepository.findByParty_PartyIdAndUser_UserId(partyId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN, "Bạn không phải là thành viên của Party này!"));
        if (pm.getRole() == null || (pm.getRole() != 0 && pm.getRole() != 1)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn cần chấp nhận lời mời gia nhập Party trước khi thực hiện thao tác này!");
        }
    }

    private NxvParties getActiveParty(Long partyId) {
        return partiesRepository.findById(partyId)
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy Party đang hoạt động với ID: " + partyId));
    }

    private Users getUserAlive(Long userId) {
        Users u = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng!"));
        if (Boolean.TRUE.equals(u.getIsDead())) {
            throw new AppException(ErrorCode.CHARACTER_DEAD);
        }
        return u;
    }
}