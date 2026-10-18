package K23CNT1.NguyenXuanVinh.nxvdto;

import K23CNT1.NguyenXuanVinh.nxventity.nxvUser; // Đã đổi tên Entity
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class nxvTopUserDTO {
    private nxvUser user;
    private BigDecimal totalAmount;
}