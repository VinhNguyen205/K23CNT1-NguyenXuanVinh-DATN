package K23CNT1.NguyenXuanVinh.nxvdto;

import lombok.Data;

@Data
public class nxvRegisterRequest { // Đã đổi tên Class
    private String username;
    private String password;
    private String fullName;
    private String email;
}