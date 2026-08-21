package vn.codegym.house_rental.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
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
    public String profile(
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        // [BỔ SUNG THEO YÊU CẦU TASK 3]: Khởi tạo UserProfile kèm Địa chỉ và đường dẫn Avatar
        UserProfile userProfile = new UserProfile(
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl()
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
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        userProfile.setUsername(currentUser.getUsername());

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "user/profile";
        }

        try {

            User updatedUser = userService.updateProfile(
                    currentUser.getUsername(),
                    userProfile
            );

            // Cập nhật lại user trong session
            session.setAttribute("currentUser", updatedUser);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật thông tin cá nhân thành công!"
            );

            return "redirect:/profile";

        } catch (IllegalArgumentException e) {

            bindingResult.rejectValue(
                    "email",
                    "error.email",
                    e.getMessage()
            );

            model.addAttribute("user", currentUser);

            return "user/profile";

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi khi cập nhật thông tin cá nhân."
            );

            model.addAttribute("user", currentUser);

            return "user/profile";
        }
    }

    // Hiển thị form thay đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePasswordForm(
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
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
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }

        try {

            userService.changePassword(
                    currentUser.getUsername(),
                    changePassword
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thay đổi mật khẩu thành công!"
            );

            return "redirect:/profile/change-password";

        } catch (IllegalArgumentException e) {

            if (e.getMessage().contains("mật khẩu hiện tại")
                    || e.getMessage().contains("Mật khẩu hiện tại")) {

                bindingResult.rejectValue(
                        "currentPassword",
                        "error.currentPassword",
                        e.getMessage()
                );

            } else if (e.getMessage().contains("xác nhận")
                    || e.getMessage().contains("Xác nhận")) {

                bindingResult.rejectValue(
                        "confirmPassword",
                        "error.confirmPassword",
                        e.getMessage()
                );

            } else {

                model.addAttribute(
                        "errorMessage",
                        e.getMessage()
                );
            }

            return "user/change-password";

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi khi thay đổi mật khẩu."
            );

            return "user/change-password";
        }
    }

    @PostMapping("/request-host")
    public String requestBecomeHost(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.requestBecomeHost(currentUser.getId());

        // [SỬA LỖI]: Cập nhật lại đối tượng currentUser trong Session để giao diện người dùng hiển thị trạng thái PENDING ngay lập tức
        userService.findById(currentUser.getId()).ifPresent(updatedUser ->
                session.setAttribute("currentUser", updatedUser)
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã gửi yêu cầu đăng ký làm chủ nhà."
        );

        return "redirect:/profile";
    }
}