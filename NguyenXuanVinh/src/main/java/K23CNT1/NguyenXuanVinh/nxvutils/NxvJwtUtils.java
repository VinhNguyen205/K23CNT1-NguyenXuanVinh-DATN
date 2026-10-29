package K23CNT1.NguyenXuanVinh.nxvutils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class NxvJwtUtils {

    @Value("${nxv.jwt.secret}")
    private String jwtSecret;

    @Value("${nxv.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    private Key signingKey;

    /** Tạo khóa 1 lần khi khởi động và báo lỗi rõ ràng nếu secret quá yếu (HS256 cần tối thiểu 32 byte). */
    @PostConstruct
    void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "nxv.jwt.secret phải dài tối thiểu 32 ký tự (hiện tại " + keyBytes.length
                    + "). Hãy đặt biến môi trường NXV_JWT_SECRET.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private Key getSigningKey() {
        return signingKey;
    }

    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString()) // jti: mỗi lần đăng nhập ra 1 token khác nhau, tránh trùng DeviceToken
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of(
                        "username", username,
                        "role", role
                ))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }
}