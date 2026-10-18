package K23CNT1.NguyenXuanVinh.nxvutils;

import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class NxvSecurityUtils {
    private NxvSecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        if (auth != null && auth.getName() != null) {
            try {
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException ignored) {}
        }
        throw new AppException(ErrorCode.UNAUTHORIZED, "Vui lÃ²ng Ä‘Äƒng nháº­p (nháº­p Bearer Token) Ä‘á»ƒ thá»±c hiá»‡n thao tÃ¡c nÃ y!");
    }
}