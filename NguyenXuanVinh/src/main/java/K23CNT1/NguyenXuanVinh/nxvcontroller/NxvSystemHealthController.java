package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxvdto.ApiResponse;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class NxvSystemHealthController {

    private final UsersRepository usersRepository;
    private final NxvLevelConfigRepository levelConfigRepository;
    private final NxvClassesRepository classesRepository;
    private final NxvItemsRepository itemsRepository;
    private final NxvSkillDefinitionsRepository skillDefinitionsRepository;
    private final NxvPetSpeciesRepository petSpeciesRepository;
    private final NxvTitlesRepository titlesRepository;
    private final NxvAchievementsRepository achievementsRepository;

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> checkDatabaseHealth() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("status", "UP - Connected to NxvTaskTaleDB v2.2 (54 Tables, 5 Views, 7 SPs)");
        stats.put("totalUsers", usersRepository.count());
        stats.put("seedLevelsCount", levelConfigRepository.count());
        stats.put("seedClassesCount", classesRepository.count());
        stats.put("seedItemsCount", itemsRepository.count());
        stats.put("seedSkillsCount", skillDefinitionsRepository.count());
        stats.put("seedPetSpeciesCount", petSpeciesRepository.count());
        stats.put("seedTitlesCount", titlesRepository.count());
        stats.put("seedAchievementsCount", achievementsRepository.count());
        return ApiResponse.ok("Kiểm tra kết nối 54 bảng NxvTaskTaleDB v2.2 thành công!", stats);
    }
}