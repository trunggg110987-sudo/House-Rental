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
import vn.codegym.house_rental.validator.RegisterValidator;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegisterValidator registerValidator;

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

        registerValidator.validate(register, bindingResult);

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
