package K23CNT1.NguyenXuanVinh.nxvconfig;

import K23CNT1.NguyenXuanVinh.nxvrepository.NxvUserSessionsRepository;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvJwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Xác thực JWT theo phiên (session-based JWT):
 *  1. Chữ ký + hạn của token hợp lệ.
 *  2. Token phải tồn tại trong NxvUserSessions với IsActive = 1 (đăng xuất / thu hồi thiết bị có hiệu lực ngay).
 *  3. Tài khoản không bị khóa; quyền (role) lấy từ DB nên đổi quyền có hiệu lực ngay, không phải chờ token hết hạn.
 */
@Component
@RequiredArgsConstructor
public class NxvJwtAuthenticationFilter extends OncePerRequestFilter {

    /** Chỉ ghi LastActiveAt khi đã quá khoảng này, tránh UPDATE DB trên mỗi request. */
    private static final Duration TOUCH_INTERVAL = Duration.ofMinutes(1);

    private final NxvJwtUtils jwtUtils;
    private final NxvUserSessionsRepository sessionsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (jwtUtils.validateToken(token)) {
                sessionsRepository.findActiveByTokenWithUser(token).ifPresent(session -> {
                    var user = session.getUser();
                    if (Boolean.TRUE.equals(user.getIsLocked())) {
                        return;
                    }

                    LocalDateTime now = LocalDateTime.now();
                    if (session.getLastActiveAt() == null
                            || Duration.between(session.getLastActiveAt(), now).compareTo(TOUCH_INTERVAL) > 0) {
                        sessionsRepository.touch(session.getSessionId(), now);
                    }

                    int role = user.getRole() == null ? 0 : user.getRole();
                    String roleName = switch (role) {
                        case 2 -> "ROLE_SUPER_ADMIN";
                        case 1 -> "ROLE_MODERATOR";
                        default -> "ROLE_USER";
                    };

                    var auth = new UsernamePasswordAuthenticationToken(
                            user.getUserId(),
                            null,
                            List.of(new SimpleGrantedAuthority(roleName)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
