package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvenums.ErrorCode;
import K23CNT1.NguyenXuanVinh.nxvexception.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Xác thực Google ID Token bằng endpoint chính thức của Google (tokeninfo).
 * Google kiểm tra chữ ký + hạn token; backend kiểm tra thêm audience (Client ID của app) và email_verified.
 * Nhờ vậy client KHÔNG thể tự khai email/ID của người khác để chiếm tài khoản.
 */
@Slf4j
@Component
public class NxvGoogleTokenVerifier {

    public record GoogleIdentity(String subject, String email, String name) {}

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private final RestClient restClient = RestClient.create();

    @Value("${nxv.google.client-id:}")
    private String clientId;

    public GoogleIdentity verify(String idToken) {
        if (!StringUtils.hasText(clientId)) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Server chưa cấu hình đăng nhập Google (nxv.google.client-id).");
        }
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(TOKENINFO_URL + "?id_token={t}", idToken)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException ex) {
            log.warn("Google tokeninfo từ chối token: {}", ex.getMessage());
            throw new AppException(ErrorCode.UNAUTHORIZED, "Google ID Token không hợp lệ hoặc đã hết hạn!");
        }
        if (body == null
                || !clientId.equals(body.path("aud").asText())
                || !"true".equalsIgnoreCase(body.path("email_verified").asText())
                || !StringUtils.hasText(body.path("sub").asText())
                || !StringUtils.hasText(body.path("email").asText())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Google ID Token không hợp lệ cho ứng dụng này!");
        }
        return new GoogleIdentity(body.path("sub").asText(),
                body.path("email").asText().trim().toLowerCase(),
                body.path("name").asText(null));
    }
}
