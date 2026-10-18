package K23CNT1.NguyenXuanVinh.nxvrepository;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface nxvUserRepository extends JpaRepository<nxvUser, Integer> {
    Optional<nxvUser> findByUsername(String username);
}