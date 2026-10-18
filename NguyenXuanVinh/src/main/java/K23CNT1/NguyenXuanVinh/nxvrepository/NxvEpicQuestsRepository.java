package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvEpicQuests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface NxvEpicQuestsRepository extends JpaRepository<NxvEpicQuests, Long> {
    List<NxvEpicQuests> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = "EXEC NxvSp_CalculateGPA @EpicQuestID = :epicQuestId", nativeQuery = true)
    BigDecimal executeCalculateGpaSp(@Param("epicQuestId") Long epicQuestId);
}