package vn.codegym.house_rental.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.dto.LoginDTO;
import vn.codegym.house_rental.dto.Register;
import vn.codegym.house_rental.model.User;
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

    @GetMapping("/login")
    public String showLoginForm(Model model) {

        LoginDTO loginDTO = new LoginDTO();
        // [BỔ SUNG THEO YÊU CẦU TASK 1]: Điền sẵn Username vừa đăng ký thành công vào form đăng nhập
        if (model.containsAttribute("registeredUsername")) {
            loginDTO.setUsername((String) model.getAttribute("registeredUsername"));
        }
        model.addAttribute("login", loginDTO);

        return "auth/login";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("register") Register register, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        registerValidator.validate(register, bindingResult);

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        userService.registerUser(register);

        // [BỔ SUNG THEO YÊU CẦU TASK 1 & 8]: Đăng ký thành công tự động chuyển sang trang đăng nhập (/login) và điền sẵn username
        redirectAttributes.addFlashAttribute("registeredUsername", register.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");

        return "redirect:/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("login") LoginDTO login,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {

            User user = userService.login(
                    login.getUsername(),
                    login.getPassword()
            );

            // Lưu user vào Session
            session.setAttribute("currentUser", user);

            return "redirect:/";

        } catch (IllegalArgumentException | IllegalStateException e) {

            model.addAttribute("errorMessage", e.getMessage());

            return "auth/login";
        }
    }

    // [CẢI TIẾN]: Bổ sung endpoint Đăng xuất để xóa sạch Session của người dùng hiện tại
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng xuất tài khoản thành công.");
        return "redirect:/login";
    }
}