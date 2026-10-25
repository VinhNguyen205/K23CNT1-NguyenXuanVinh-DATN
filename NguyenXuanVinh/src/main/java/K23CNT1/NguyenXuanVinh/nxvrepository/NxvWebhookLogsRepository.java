package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvWebhookLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvWebhookLogsRepository extends JpaRepository<NxvWebhookLogs, Long> {
    List<NxvWebhookLogs> findTop100ByOrderByCreatedAtDesc();
    List<NxvWebhookLogs> findByOrderCodeOrderByCreatedAtDesc(String orderCode);
}