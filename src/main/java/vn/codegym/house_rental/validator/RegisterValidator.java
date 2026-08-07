package vn.codegym.house_rental.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import vn.codegym.house_rental.dto.Register;
import vn.codegym.house_rental.service.UserService;

@Component
public class RegisterValidator implements Validator {
    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Register.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Register register = (Register) target;

        // ktra nat khau
        if (register.getPassword() != null && !register.getPassword().equals(register.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        // ktra trung ten dang nhap
        if (register.getUsername() != null && userService.findByUsername(register.getUsername()).isPresent()) {
            errors.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại trong hệ thống");
        }

        // ktra trung email
        if (register.getEmail() != null && userService.existsByEmail(register.getEmail())) {
            errors.rejectValue("email", "error.email", "Email này đã được đăng ký bởi tài khoản khác");
        }
    }
}