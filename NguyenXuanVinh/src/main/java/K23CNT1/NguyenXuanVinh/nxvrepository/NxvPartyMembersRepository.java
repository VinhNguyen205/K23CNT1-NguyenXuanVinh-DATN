package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvPartyMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NxvPartyMembersRepository extends JpaRepository<NxvPartyMembers, Long> {
    List<NxvPartyMembers> findByParty_PartyIdOrderByRoleAscJoinedAtAsc(Long partyId);
    List<NxvPartyMembers> findByUser_UserIdOrderByJoinedAtDesc(Long userId);
    Optional<NxvPartyMembers> findByParty_PartyIdAndUser_UserId(Long partyId, Long userId);
    long countByParty_PartyIdAndRoleIn(Long partyId, Collection<Integer> roles);
}