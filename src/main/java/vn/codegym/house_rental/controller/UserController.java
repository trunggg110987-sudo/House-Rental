package vn.codegym.house_rental.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.dto.ChangePassword;
import vn.codegym.house_rental.dto.UserProfile;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.UserService;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserController {

    @Autowired
    private UserService userService;

    // Xem thông tin profile cá nhân
    @GetMapping
    public String showProfile(Model model) {
        Optional<User> userOptional = userService.findByUsername("user1");
        if (userOptional.isEmpty()) {
            return "redirect:/";
        }

        User currentUser = userOptional.get();
        UserProfile userProfile = new UserProfile(
                currentUser.getUsername(),
                currentUser.getFullName(),
                currentUser.getEmail(),
                currentUser.getPhone()
        );

        model.addAttribute("userProfile", userProfile);
        return "user/profile";
    }

    // Cập nhật thông tin profile cá nhân
    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("userProfile") UserProfile userProfile,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        userProfile.setUsername("user1");

        if (bindingResult.hasErrors()) {
            return "user/profile";
        }

        try {
            userService.updateProfile("user1", userProfile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "error.email", e.getMessage());
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật thông tin cá nhân.");
            return "user/profile";
        }
    }

    // Hiển thị form thay đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePassword", new ChangePassword());
        return "user/change_password";
    }

    // Xử lý thay đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePassword") ChangePassword changePassword,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "user/change_password";
        }

        try {
            userService.changePassword("user1", changePassword);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi mật khẩu thành công!");
            return "redirect:/profile/change-password";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("mật khẩu hiện tại") || e.getMessage().contains("Mật khẩu hiện tại")) {
                bindingResult.rejectValue("currentPassword", "error.currentPassword", e.getMessage());
            } else if (e.getMessage().contains("xác nhận") || e.getMessage().contains("Xác nhận")) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", e.getMessage());
            } else {
                model.addAttribute("errorMessage", e.getMessage());
            }
            return "user/change_password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi thay đổi mật khẩu.");
            return "user/change_password";
        }
    }
}