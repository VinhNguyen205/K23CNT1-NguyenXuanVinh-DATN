package K23CNT1.NguyenXuanVinh.nxvdto;

import K23CNT1.NguyenXuanVinh.nxventity.nxvUserInventory;
import lombok.AllArgsConstructor; // Thêm
import lombok.Builder;          // Thêm
import lombok.Data;
import lombok.NoArgsConstructor; // Thêm
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class nxvOpenBoxResponse {
    private String itemName;
    private String itemImage;
    private String rarity;
    private Integer spinsUntilPity;
    private BigDecimal currentBalance;
    private boolean isPityTriggered;
    private nxvUserInventory inventoryItem;
}