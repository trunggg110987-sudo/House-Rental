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

    // Lấy user đang đăng nhập từ session
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute(LoginController.SESSION_USER_KEY);
    }

    // Xem thông tin profile cá nhân
    @GetMapping
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User sessionUser = getCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem trang cá nhân");
            return "redirect:/login";
        }

        // Lấy lại dữ liệu mới nhất từ DB (tránh dữ liệu cũ trong session)
        Optional<User> userOptional = userService.findByUsername(sessionUser.getUsername());
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
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User sessionUser = getCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục");
            return "redirect:/login";
        }

        String username = sessionUser.getUsername();
        userProfile.setUsername(username);

        if (bindingResult.hasErrors()) {
            return "user/profile";
        }

        try {
            User updatedUser = userService.updateProfile(username, userProfile);

            // Cập nhật lại session để header/các trang khác hiển thị đúng ngay lập tức
            session.setAttribute(LoginController.SESSION_USER_KEY, updatedUser);

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
    public String showChangePasswordForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (getCurrentUser(session) == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục");
            return "redirect:/login";
        }
        model.addAttribute("changePassword", new ChangePassword());
        return "user/change-password";
    }

    // Xử lý thay đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePassword") ChangePassword changePassword,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        User sessionUser = getCurrentUser(session);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiếp tục");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }

        String username = sessionUser.getUsername();

        try {
            userService.changePassword(username, changePassword);
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
            return "user/change-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi thay đổi mật khẩu.");
            return "user/change-password";
        }
    }
}