package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvVwOverdueTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NxvVwOverdueTasksRepository extends JpaRepository<NxvVwOverdueTasks, Long> {
}