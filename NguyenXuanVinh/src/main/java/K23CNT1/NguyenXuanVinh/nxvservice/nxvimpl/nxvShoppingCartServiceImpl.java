package K23CNT1.NguyenXuanVinh.nxvservice.nxvimpl;

import K23CNT1.NguyenXuanVinh.nxventity.*;
import K23CNT1.NguyenXuanVinh.nxvrepository.*;
import K23CNT1.NguyenXuanVinh.nxvservice.nxvShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class nxvShoppingCartServiceImpl implements nxvShoppingCartService {
    @Autowired private nxvCartRepository nxvCartRepository;
    @Autowired private nxvCartItemRepository nxvCartItemRepository;
    @Autowired private nxvUserRepository nxvUserRepository;
    @Autowired private nxvBlindBoxRepository nxvBlindBoxRepository;

    @Override
    @Transactional
    public void addBoxToCart(Integer userId, Integer boxId, int quantity) {
        nxvUser user = nxvUserRepository.findById(userId).orElseThrow();
        nxvBlindBox box = nxvBlindBoxRepository.findById(boxId).orElseThrow();

        nxvCart cart = nxvCartRepository.findByUser(user).orElseGet(() -> {
            nxvCart c = new nxvCart();
            c.setUser(user);
            c.setCreatedAt(LocalDateTime.now());
            return nxvCartRepository.save(c);
        });

        Optional<nxvCartItem> existing = nxvCartItemRepository.findByCart(cart).stream()
                .filter(i -> i.getBlindBox() != null && i.getBlindBox().getBoxId().equals(boxId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
            nxvCartItemRepository.save(existing.get());
        } else {
            nxvCartItem newItem = new nxvCartItem();
            newItem.setCart(cart);
            newItem.setBlindBox(box);
            newItem.setQuantity(quantity);
            newItem.setAddedAt(LocalDateTime.now());
            nxvCartItemRepository.save(newItem);
        }
    }

    @Override
    public nxvCart getCart(Integer userId) {
        return nxvCartRepository.findByUser(nxvUserRepository.findById(userId).orElse(null)).orElse(null);
    }

    @Override
    public void removeCartItem(Integer cartItemId) {
        nxvCartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Integer userId) {
        nxvCartRepository.findByUser(nxvUserRepository.findById(userId).orElse(null))
                .ifPresent(c -> nxvCartItemRepository.deleteByCart(c));
    }
}