package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    private String address;

    // [BỔ SUNG THEO YÊU CẦU TASK 3]: Bổ sung đường dẫn ảnh đại diện avatarUrl (mặc định nếu null)
    @Builder.Default
    private String avatarUrl = "https://www.magnific.com/icon/user_4596193";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private HostStatus hostStatus = HostStatus.NONE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public enum Role {
        ROLE_USER,
        ROLE_HOST,
        ROLE_ADMIN
    }

    public enum HostStatus {
        NONE,
        PENDING,
        APPROVED,
        REJECTED
    }
}