package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxventity.Users;
import K23CNT1.NguyenXuanVinh.nxvrepository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NxvLoginAttemptService {

    private final UsersRepository usersRepository;
    public static final int MAX_FAIL_ATTEMPTS = 5;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedLogin(Long userId) {
        Users user = usersRepository.findById(userId).orElseThrow();
        int fails = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        user.setLoginFailCount(fails);
        if (fails >= MAX_FAIL_ATTEMPTS) {
            user.setIsLocked(true);
        }
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
        return fails;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedLogin(Long userId) {
        Users user = usersRepository.findById(userId).orElseThrow();
        user.setLoginFailCount(0);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);
    }
}