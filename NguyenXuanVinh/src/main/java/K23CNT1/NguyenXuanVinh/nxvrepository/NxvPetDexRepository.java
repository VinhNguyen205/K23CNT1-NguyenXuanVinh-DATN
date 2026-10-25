package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvPetDex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NxvPetDexRepository extends JpaRepository<NxvPetDex, Long> {
    List<NxvPetDex> findByUser_UserIdOrderByFirstObtainedAtDesc(Long userId);
    boolean existsByUser_UserIdAndSpecies_SpeciesId(Long userId, Integer speciesId);
}