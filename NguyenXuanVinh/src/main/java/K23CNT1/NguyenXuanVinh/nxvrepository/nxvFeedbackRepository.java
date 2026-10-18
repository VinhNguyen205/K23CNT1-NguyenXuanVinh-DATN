package K23CNT1.NguyenXuanVinh.nxvrepository;
import K23CNT1.NguyenXuanVinh.nxventity.nxvFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface nxvFeedbackRepository extends JpaRepository<nxvFeedback, Integer> {
    List<nxvFeedback> findAllByOrderBySentAtDesc(); // Phản hồi mới nhất lên đầu
}