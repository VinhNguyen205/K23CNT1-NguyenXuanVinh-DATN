package K23CNT1.NguyenXuanVinh.nxvutils;

import K23CNT1.NguyenXuanVinh.nxvservice.NxvTaskQuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class NxvTaskDeadlineScheduler {

    private final NxvTaskQuestService taskQuestService;

    // Chuc nang #36: Tu dong chay vao 00:05 moi dem de danh dau Fail cac Task qua han & tru HP
    @Scheduled(cron = "0 5 0 * * *")
    public void runNightlyDeadlineCheck() {
        log.info("[TaskTale Cronjob] Bat dau quet cac Task qua han va tru HP...");
        var result = taskQuestService.executeNightlyDeadlineCronAndReminders();
        log.info("[TaskTale Cronjob] Hoan tat: {}", result);
    }
}