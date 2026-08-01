package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.dto.ChangePassword;
import vn.codegym.house_rental.dto.Register;
import vn.codegym.house_rental.dto.UserProfile;
import vn.codegym.house_rental.exception.UserRegistrationException;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.UserRepository;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
        //ktra email đã tồn tại
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
        // ktra email đã tồn tại ở tài khoản khác id
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    public User registerUser(Register register) {
        try {
            User user = User.builder()
                    .username(register.getUsername())
                    .password(register.getPassword())
                    .fullName(register.getFullName())
                    .email(register.getEmail())
                    .phone(register.getPhone())
                    .role(User.Role.ROLE_USER)
                    .build();
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserRegistrationException("Dữ liệu đăng ký không hợp lệ hoặc đã tồn tại trong hệ thống.", e);
        } catch (Exception e) {
            throw new UserRegistrationException("Đã xảy ra lỗi hệ thống khi đăng ký tài khoản.", e);
        }
    }

    public User updateProfile(String username, UserProfile userProfile) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (userRepository.existsByEmailAndIdNot(userProfile.getEmail(), user.getId())) {
            throw new IllegalArgumentException("Email này đã được sử dụng bởi tài khoản khác");
        }

        user.setFullName(userProfile.getFullName());
        user.setEmail(userProfile.getEmail());
        user.setPhone(userProfile.getPhone());

        return userRepository.save(user);
    }

    public User changePassword(String username, ChangePassword changePasswordDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // ktra mk hiện tại
        if (!user.getPassword().equals(changePasswordDto.getCurrentPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        // ktra mk mới và xác nhận mk có trùng nhau ko
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp với mật khẩu mới");
        }

        // cập nhật mk mới
        user.setPassword(changePasswordDto.getNewPassword());
        return userRepository.save(user);
    }
}