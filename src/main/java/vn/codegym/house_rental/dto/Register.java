package vn.codegym.house_rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Register {

    // [BỔ SUNG THEO YÊU CẦU TASK 2 & 7]: Validate tên đăng nhập từ 4-50 ký tự và không chứa ký tự đặc biệt
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập không được chứa ký tự đặc biệt hoặc khoảng trắng")
    private String username;

    // [BỔ SUNG THEO YÊU CẦU TASK 7]: Validate mật khẩu từ 6-32 ký tự
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 32, message = "Mật khẩu phải từ 6 đến 32 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    // [BỔ SUNG THEO YÊU CẦU TASK 2]: Validate họ tên không chứa ký tự đặc biệt
    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ỹ]+$", message = "Họ và tên không được chứa ký tự đặc biệt")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    // [BỔ SUNG THEO YÊU CẦU TASK 2]: Validate số điện thoại
    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải từ 10-11 chữ số và không chứa ký tự đặc biệt")
    private String phone;

    // [BỔ SUNG THEO YÊU CẦU TASK 7]: Đánh dấu đăng ký làm Chủ nhà (isHost = true/false)
    private boolean isHost = false;

    public Register() {
    }

    public Register(String username, String password, String confirmPassword, String email, String fullName, String phone) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // [BỔ SUNG THEO YÊU CẦU TASK 7]: Getter và Setter cho cờ isHost
    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}