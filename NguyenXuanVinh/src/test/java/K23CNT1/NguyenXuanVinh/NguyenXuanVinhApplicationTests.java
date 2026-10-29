package K23CNT1.NguyenXuanVinh;

import K23CNT1.NguyenXuanVinh.nxvutils.NxvJwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test thuần (không cần SQL Server). Test contextLoads cũ đã bỏ vì cần kết nối DB thật nên làm `mvn package` bị lỗi
 * khi chưa bật SQL Server; có thể thêm lại @SpringBootTest khi có DB test riêng.
 */
class NguyenXuanVinhApplicationTests {

    private NxvJwtUtils newJwtUtils() {
        NxvJwtUtils utils = new NxvJwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", "unit-test-secret-key-that-is-at-least-32-bytes-long!");
        ReflectionTestUtils.setField(utils, "jwtExpirationMs", 60_000L);
        ReflectionTestUtils.invokeMethod(utils, "init");
        return utils;
    }

    @Test
    void tokenRoundTripKeepsUserId() {
        NxvJwtUtils utils = newJwtUtils();
        String token = utils.generateToken(42L, "vinh", 0);
        assertTrue(utils.validateToken(token));
        assertEquals(42L, utils.getUserIdFromToken(token));
    }

    @Test
    void twoTokensForSameUserAreDifferent() {
        NxvJwtUtils utils = newJwtUtils();
        assertNotEquals(utils.generateToken(1L, "a", 0), utils.generateToken(1L, "a", 0));
    }

    @Test
    void tamperedTokenIsRejected() {
        NxvJwtUtils utils = newJwtUtils();
        String token = utils.generateToken(1L, "a", 0);
        assertFalse(utils.validateToken(token.substring(0, token.length() - 2) + "xx"));
    }

    @Test
    void shortSecretFailsFast() {
        NxvJwtUtils utils = new NxvJwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", "too-short");
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(utils, "init"));
    }
}
