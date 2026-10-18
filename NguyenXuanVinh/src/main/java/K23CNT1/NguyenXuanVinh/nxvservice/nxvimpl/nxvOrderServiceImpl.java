package K23CNT1.NguyenXuanVinh.nxvservice.nxvimpl;

import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvOrderService;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class nxvOrderServiceImpl implements nxvOrderService {
    @Autowired private nxvCartRepository nxvCartRepository;
    @Autowired private nxvCartItemRepository nxvCartItemRepository;
    @Autowired private nxvOrderRepository nxvOrderRepository;
    @Autowired private nxvOrderDetailRepository nxvOrderDetailRepository;
    @Autowired private nxvUserRepository nxvUserRepository;
    @Autowired private nxvShoppingCartService nxvShoppingCartService;
    @Autowired private nxvBoxItemRepository nxvBoxItemRepository;
    @Autowired private nxvUserInventoryRepository nxvUserInventoryRepository;
    @Autowired private nxvUserPityStatRepository nxvUserPityStatRepository;
    @Autowired private nxvTransactionRepository nxvTransactionRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public nxvOrder placeOrder(Integer userId, String receiverName, String phoneNumber, String address) {
        nxvUser user = nxvUserRepository.findById(userId).orElseThrow();
        nxvCart cart = nxvCartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Giỏ trống"));
        List<nxvCartItem> items = nxvCartItemRepository.findByCart(cart);
        if (items.isEmpty()) throw new RuntimeException("Giỏ trống");

        BigDecimal total = BigDecimal.ZERO;
        for (nxvCartItem i : items) {
            if (i.getBlindBox() != null) total = total.add(i.getBlindBox().getPrice().multiply(new BigDecimal(i.getQuantity())));
        }

        if (user.getWalletBalance().compareTo(total) < 0) throw new RuntimeException("Không đủ tiền");
        user.setWalletBalance(user.getWalletBalance().subtract(total));
        nxvUserRepository.save(user);

        nxvTransaction trans = new nxvTransaction();
        trans.setUser(user);
        trans.setAmount(total.negate());
        trans.setTransactionType("ORDER_PAYMENT");
        nxvTransactionRepository.save(trans);

        nxvOrder order = new nxvOrder();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(total);
        order.setReceiverName(receiverName);
        order.setPhoneNumber(phoneNumber);
        order.setShippingAddress(address);
        order.setOrderStatus("COMPLETED");
        nxvOrder savedOrder = nxvOrderRepository.save(order);

        for (nxvCartItem item : items) {
            if (item.getBlindBox() != null) {
                nxvBlindBox box = item.getBlindBox();
                nxvOrderDetail detail = new nxvOrderDetail();
                detail.setOrder(savedOrder);
                detail.setBlindBox(box);
                detail.setQuantity(item.getQuantity());
                detail.setPriceAtTime(box.getPrice());
                nxvOrderDetailRepository.save(detail);

                List<nxvBoxItem> boxItems = nxvBoxItemRepository.findByBlindBox(box);
                for (int i = 0; i < item.getQuantity(); i++) {
                    nxvUserPityStat pityStat = nxvUserPityStatRepository.findByUserAndBlindBox(user, box)
                            .orElseGet(() -> {
                                nxvUserPityStat s = new nxvUserPityStat();
                                s.setUser(user); s.setBlindBox(box); s.setSpinsWithoutS(0);
                                return nxvUserPityStatRepository.save(s);
                            });

                    nxvBoxItem selected;
                    if (pityStat.getSpinsWithoutS() >= 50) {
                        selected = boxItems.stream().filter(b -> "S".equals(b.getRarityLevel())).findFirst().orElse(boxItems.get(0));
                        pityStat.setSpinsWithoutS(0);
                    } else {
                        selected = performRandomDrop(boxItems);
                        if ("S".equals(selected.getRarityLevel())) pityStat.setSpinsWithoutS(0);
                        else pityStat.setSpinsWithoutS(pityStat.getSpinsWithoutS() + 1);
                    }
                    nxvUserPityStatRepository.save(pityStat);

                    nxvUserInventory inv = new nxvUserInventory();
                    inv.setUser(user); inv.setBoxItem(selected); inv.setStatus("IN_STORAGE");
                    nxvUserInventoryRepository.save(inv);
                }
            }
        }
        nxvShoppingCartService.clearCart(userId);
        return savedOrder;
    }

    private nxvBoxItem performRandomDrop(List<nxvBoxItem> items) {
        double total = items.stream().mapToDouble(i -> i.getProbability() != null ? i.getProbability() : 0).sum();
        double r = random.nextDouble() * total;
        double current = 0;
        for (nxvBoxItem i : items) {
            current += i.getProbability() != null ? i.getProbability() : 0;
            if (r <= current) return i;
        }
        return items.get(items.size() - 1);
    }
}