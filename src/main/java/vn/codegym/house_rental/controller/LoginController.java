package vn.codegym.house_rental.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.dto.LoginForm;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.UserService;

import java.util.Optional;

@Controller
public class LoginController {

    public static final String SESSION_USER_KEY = "loggedInUser";

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login"; // src/main/resources/templates/auth/login.html
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        Optional<User> userOpt = userService.findByUsername(loginForm.getUsername());

        // So sánh plain text, đồng bộ với cách hệ thống đang lưu password
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(loginForm.getPassword())) {
            model.addAttribute("loginError", "Tên đăng nhập hoặc mật khẩu không chính xác");
            return "auth/login";
        }

        session.setAttribute(SESSION_USER_KEY, userOpt.get());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}