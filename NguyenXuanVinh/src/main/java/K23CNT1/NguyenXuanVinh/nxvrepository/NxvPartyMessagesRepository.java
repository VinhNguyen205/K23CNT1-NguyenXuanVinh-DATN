package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvPartyMessages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvPartyMessagesRepository extends JpaRepository<NxvPartyMessages, Long> {
    List<NxvPartyMessages> findTop100ByParty_PartyIdOrderByCreatedAtDesc(Long partyId);
}