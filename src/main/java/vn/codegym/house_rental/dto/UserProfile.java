package vn.codegym.house_rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserProfile {

    private String username;

    // [BỔ SUNG THEO YÊU CẦU TASK 2 & 3]: Validate họ tên không ký tự đặc biệt
    @NotBlank(message = "Họ và tên không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9\\sÀ-ỹ]+$", message = "Họ và tên không được chứa ký tự đặc biệt")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    // [BỔ SUNG THEO YÊU CẦU TASK 2]: Validate số điện thoại
    @Pattern(regexp = "^$|^[0-9]{10,11}$", message = "Số điện thoại phải từ 10-11 chữ số và không chứa ký tự đặc biệt")
    private String phone;

    // [BỔ SUNG THEO YÊU CẦU TASK 3]: Bổ sung trường địa chỉ và avatarUrl cho Profile
    private String address;
    private String avatarUrl;

    public UserProfile() {
    }

    public UserProfile(String username, String fullName, String email, String phone, String address, String avatarUrl) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}