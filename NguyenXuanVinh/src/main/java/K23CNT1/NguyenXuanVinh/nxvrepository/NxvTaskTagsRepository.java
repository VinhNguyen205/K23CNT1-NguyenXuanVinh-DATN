package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvTaskTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NxvTaskTagsRepository extends JpaRepository<NxvTaskTags, Long> {
    List<NxvTaskTags> findByTask_TaskId(Long taskId);
    List<NxvTaskTags> findByTask_User_UserId(Long userId);

    @Transactional
    void deleteByTask_TaskId(Long taskId);
}