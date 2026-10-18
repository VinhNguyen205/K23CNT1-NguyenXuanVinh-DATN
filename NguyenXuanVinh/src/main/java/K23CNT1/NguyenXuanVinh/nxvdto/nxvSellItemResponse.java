package K23CNT1.NguyenXuanVinh.nxvdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class nxvSellItemResponse {
    private String message;
    private String itemName;
    private BigDecimal soldPrice;
    private BigDecimal newBalance;
}