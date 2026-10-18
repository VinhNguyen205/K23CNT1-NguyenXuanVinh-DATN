package K23CNT1.NguyenXuanVinh.nxvcontroller;

import K23CNT1.NguyenXuanVinh.nxventity.nxvTransaction;
import K23CNT1.NguyenXuanVinh.nxventity.nxvUser;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvTransactionRepository;
import K23CNT1.NguyenXuanVinh.nxvrepository.nxvUserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class nxvPaymentController {

    private final nxvUserRepository nxvUserRepository;
    private final nxvTransactionRepository nxvTransactionRepository;

    // 1. Hiển thị trang nạp tiền
    @GetMapping("/deposit")
    public String showDepositPage(HttpSession session, Model model) {
        nxvUser currentUser = (nxvUser) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        // Cập nhật lại số dư mới nhất từ DB để hiển thị
        nxvUser user = nxvUserRepository.findById(currentUser.getUserId()).orElse(null);
        model.addAttribute("nxvUser", user);
        return "deposit"; // Trả về file deposit.html
    }

    // 2. Xử lý nạp tiền (Giả lập thành công ngay lập tức)
    @PostMapping("/api/deposit/process")
    public String processDeposit(@RequestParam("amount") BigDecimal amount,
                                 @RequestParam("method") String method,
                                 HttpSession session) {

        nxvUser sessionUser = (nxvUser) session.getAttribute("currentUser");
        if (sessionUser == null) return "redirect:/login";

        // Tìm user trong DB
        nxvUser user = nxvUserRepository.findById(sessionUser.getUserId()).orElse(null);
        if (user != null) {
            // Cộng tiền vào ví
            user.setWalletBalance(user.getWalletBalance().add(amount));
            nxvUserRepository.save(user);

            // Lưu lịch sử giao dịch
            nxvTransaction trans = new nxvTransaction();
            trans.setUser(user);
            trans.setAmount(amount);
            trans.setTransactionType("DEPOSIT");
            trans.setDescription("Nạp xu qua " + method);
            trans.setTransactionDate(LocalDateTime.now());
            nxvTransactionRepository.save(trans);
            // Cập nhật lại session
            session.setAttribute("currentUser", user);
        }

        return "redirect:/deposit?success";
    }
}