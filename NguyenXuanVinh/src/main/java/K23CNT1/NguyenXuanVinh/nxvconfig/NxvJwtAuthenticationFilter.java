package K23CNT1.NguyenXuanVinh.nxvconfig;

import K23CNT1.NguyenXuanVinh.nxvrepository.NxvUserSessionsRepository;
import K23CNT1.NguyenXuanVinh.nxvutils.NxvJwtUtils;
import io.jsonwebtoken.Claims;
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
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NxvJwtAuthenticationFilter extends OncePerRequestFilter {

    private final NxvJwtUtils jwtUtils;
    private final NxvUserSessionsRepository sessionsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtils.validateToken(token)) {
                var sessionOpt = sessionsRepository.findByDeviceTokenAndIsActiveTrue(token);
                if (sessionOpt.isPresent()) {
                    var session = sessionOpt.get();
                    session.setLastActiveAt(LocalDateTime.now());
                    sessionsRepository.save(session);

                    Claims claims = jwtUtils.parseClaims(token);
                    Long userId = Long.parseLong(claims.getSubject());
                    Integer role = claims.get("role", Integer.class);
                    String roleName = (role != null && role == 2) ? "ROLE_SUPER_ADMIN"
                            : (role != null && role == 1) ? "ROLE_MODERATOR" : "ROLE_USER";

                    var auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            token,
                            List.of(new SimpleGrantedAuthority(roleName))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}