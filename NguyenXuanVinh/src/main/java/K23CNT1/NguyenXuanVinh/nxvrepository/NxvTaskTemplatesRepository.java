package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvTaskTemplates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NxvTaskTemplatesRepository extends JpaRepository<NxvTaskTemplates, Long> {
    List<NxvTaskTemplates> findByUser_UserIdOrderByTemplateIdDesc(Long userId);
    Optional<NxvTaskTemplates> findByTemplateIdAndUser_UserId(Long templateId, Long userId);
}