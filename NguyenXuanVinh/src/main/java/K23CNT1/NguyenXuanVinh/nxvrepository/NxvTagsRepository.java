package K23CNT1.NguyenXuanVinh.nxvrepository;

import K23CNT1.NguyenXuanVinh.nxventity.NxvTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NxvTagsRepository extends JpaRepository<NxvTags, Integer> {
    Optional<NxvTags> findByTagNameIgnoreCase(String tagName);
    boolean existsByTagNameIgnoreCase(String tagName);
}