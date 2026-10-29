package K23CNT1.NguyenXuanVinh.nxvenums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SUCCESS(200, "Thao tác thành công", HttpStatus.OK),
    BAD_REQUEST(400, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "Chưa xác thực hoặc phiên đăng nhập hết hạn", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "Không tìm thấy dữ liệu yêu cầu", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(405, "Phương thức HTTP không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(409, "Dữ liệu bị trùng hoặc xung đột", HttpStatus.CONFLICT),
    TOO_MANY_REQUESTS(429, "Bạn thao tác quá nhanh, vui lòng thử lại sau", HttpStatus.TOO_MANY_REQUESTS),

    // Stored Procedure Error Codes (v2.2)
    TASK_NOT_FOUND(50010, "Task không tồn tại.", HttpStatus.NOT_FOUND),
    TASK_ALREADY_PROCESSED(50011, "Task đã xử lý rồi.", HttpStatus.BAD_REQUEST),
    CHARACTER_DEAD(50012, "Nhân vật đang chết, không thể làm việc.", HttpStatus.FORBIDDEN),
    ITEM_NOT_EQUIPMENT(50100, "Vật phẩm không phải trang bị", HttpStatus.BAD_REQUEST),
    ITEM_NOT_OWNED(50101, "Không sở hữu vật phẩm này", HttpStatus.BAD_REQUEST),
    ITEM_NOT_CONSUMABLE(50102, "Không phải vật phẩm tiêu hao", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_ITEM_QTY(50103, "Không đủ vật phẩm trong túi đồ", HttpStatus.BAD_REQUEST),
    SLOT_EMPTY(50104, "Slot này hiện không có trang bị nào", HttpStatus.BAD_REQUEST),
    DEAD_CANNOT_FOCUS(50200, "Nhân vật đang chết, không thể tích lũy thời gian tập trung.", HttpStatus.FORBIDDEN),

    INSUFFICIENT_GOLD(4001, "Không đủ Vàng (Gold) để thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_MP(4002, "Không đủ Năng lượng (MP) để kích hoạt kỹ năng", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(500, "Lỗi hệ thống nội bộ", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}