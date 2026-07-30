package vn.codegym.house_rental.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.dto.Register;
import vn.codegym.house_rental.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("register", new Register());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("register") Register register,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1. Kiểm tra mật khẩu xác nhận
        if (register.getPassword() != null && !register.getPassword().equals(register.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        // 2. Kiểm tra trùng tên đăng nhập
        if (register.getUsername() != null && userService.findByUsername(register.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại trong hệ thống");
        }

        // 3. Kiểm tra trùng email
        if (register.getEmail() != null && userService.existsByEmail(register.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email này đã được đăng ký bởi tài khoản khác");
        }

        // 4. Nếu có bất kỳ lỗi nào -> trả về trang đăng ký
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // 5. Lưu người dùng mới
        userService.registerUser(register);

        // 6. Thông báo thành công & chuyển hướng
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Bạn có thể sử dụng tài khoản mới.");
        return "redirect:/";
    }
}
