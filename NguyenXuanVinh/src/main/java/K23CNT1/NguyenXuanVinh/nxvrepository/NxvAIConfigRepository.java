package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvAIConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NxvAIConfigRepository extends JpaRepository<NxvAIConfig, Integer> {
    Optional<NxvAIConfig> findFirstByIsActiveTrueOrderByUpdatedAtDesc();
}