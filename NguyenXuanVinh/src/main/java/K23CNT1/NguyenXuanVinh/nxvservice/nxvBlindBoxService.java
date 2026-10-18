package K23CNT1.NguyenXuanVinh.nxvservice;

import K23CNT1.NguyenXuanVinh.nxvdto.nxvOpenBoxResponse;
import K23CNT1.NguyenXuanVinh.nxvdto.nxvSellItemResponse;

public interface nxvBlindBoxService {
    nxvOpenBoxResponse buyAndOpenBox(Integer userId, Integer boxId);
    nxvSellItemResponse sellItem(Integer userId, Integer inventoryId);
}