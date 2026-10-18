package K23CNT1.NguyenXuanVinh.nxvenums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    SUCCESS(200, "Thao tÃ¡c thÃ nh cÃ´ng", HttpStatus.OK),
    BAD_REQUEST(400, "YÃªu cáº§u khÃ´ng há»£p lá»‡", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "ChÆ°a xÃ¡c thá»±c hoáº·c phiÃªn Ä‘Äƒng nháº­p háº¿t háº¡n", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "Báº¡n khÃ´ng cÃ³ quyá»n thá»±c hiá»‡n thao tÃ¡c nÃ y", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "KhÃ´ng tÃ¬m tháº¥y dá»¯ liá»‡u yÃªu cáº§u", HttpStatus.NOT_FOUND),

    // Stored Procedure Error Codes (v2.2)
    TASK_NOT_FOUND(50010, "Task khÃ´ng tá»“n táº¡i.", HttpStatus.NOT_FOUND),
    TASK_ALREADY_PROCESSED(50011, "Task Ä‘Ã£ xá»­ lÃ½ rá»“i.", HttpStatus.BAD_REQUEST),
    CHARACTER_DEAD(50012, "NhÃ¢n váº­t Ä‘ang cháº¿t, khÃ´ng thá»ƒ lÃ m viá»‡c.", HttpStatus.FORBIDDEN),
    ITEM_NOT_EQUIPMENT(50100, "Váº­t pháº©m khÃ´ng pháº£i trang bá»‹", HttpStatus.BAD_REQUEST),
    ITEM_NOT_OWNED(50101, "KhÃ´ng sá»Ÿ há»¯u váº­t pháº©m nÃ y", HttpStatus.BAD_REQUEST),
    ITEM_NOT_CONSUMABLE(50102, "KhÃ´ng pháº£i váº­t pháº©m tiÃªu hao", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_ITEM_QTY(50103, "KhÃ´ng Ä‘á»§ váº­t pháº©m trong tÃºi Ä‘á»“", HttpStatus.BAD_REQUEST),
    SLOT_EMPTY(50104, "Slot nÃ y hiá»‡n khÃ´ng cÃ³ trang bá»‹ nÃ o", HttpStatus.BAD_REQUEST),
    DEAD_CANNOT_FOCUS(50200, "NhÃ¢n váº­t Ä‘ang cháº¿t, khÃ´ng thá»ƒ tÃ­ch lÅ©y thá»i gian táº­p trung.", HttpStatus.FORBIDDEN),

    INSUFFICIENT_GOLD(4001, "KhÃ´ng Ä‘á»§ VÃ ng (Gold) Ä‘á»ƒ thá»±c hiá»‡n giao dá»‹ch", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_MP(4002, "KhÃ´ng Ä‘á»§ NÄƒng lÆ°á»£ng (MP) Ä‘á»ƒ kÃ­ch hoáº¡t ká»¹ nÄƒng", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(500, "Lá»—i há»‡ thá»‘ng ná»™i bá»™", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}