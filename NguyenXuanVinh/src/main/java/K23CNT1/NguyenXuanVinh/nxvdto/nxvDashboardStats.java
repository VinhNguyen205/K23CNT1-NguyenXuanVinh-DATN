package K23CNT1.NguyenXuanVinh.nxvdto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class nxvDashboardStats { // Đã đổi tên Class
    private long totalUsers;
    private BigDecimal totalDeposit;
    private BigDecimal totalSpent;
}