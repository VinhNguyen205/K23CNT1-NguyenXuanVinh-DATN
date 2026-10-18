package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvCharacters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NxvCharactersRepository extends JpaRepository<NxvCharacters, Long> {
    Optional<NxvCharacters> findByUser_UserId(Long userId);
}