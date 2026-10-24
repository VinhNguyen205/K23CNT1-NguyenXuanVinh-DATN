package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvSubTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvSubTasksRepository extends JpaRepository<NxvSubTasks, Long> {
    List<NxvSubTasks> findByEpicQuest_EpicQuestIdOrderByCreatedAtAsc(Long epicQuestId);
    Optional<NxvSubTasks> findBySubTaskIdAndEpicQuest_User_UserId(Long subTaskId, Long userId);
}