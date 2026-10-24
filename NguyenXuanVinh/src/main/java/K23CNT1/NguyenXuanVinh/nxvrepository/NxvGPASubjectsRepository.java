package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvGPASubjects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvGPASubjectsRepository extends JpaRepository<NxvGPASubjects, Long> {
    List<NxvGPASubjects> findByEpicQuest_EpicQuestIdOrderBySubjectIdAsc(Long epicQuestId);
    Optional<NxvGPASubjects> findBySubjectIdAndEpicQuest_User_UserId(Long subjectId, Long userId);
}