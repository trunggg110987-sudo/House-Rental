package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import vn.codegym.house_rental.dto.UserDetailDTO;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.dto.HostDTO;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.NotificationService;
import vn.codegym.house_rental.service.UserService;
import vn.codegym.house_rental.service.EmailService;
import vn.codegym.house_rental.dto.HostDetailDTO;
import vn.codegym.house_rental.model.House;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;


    @GetMapping("/users")
    public String showUsers(@RequestParam(defaultValue = "0") int page, Model model) {

        Page<User> userPage = userService.getAllUsers(PageRequest.of(page, 5));

        model.addAttribute("users", userPage);

        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {

        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        UserDetailDTO dto = new UserDetailDTO();

        dto.setUser(user);

        dto.setTotalSpent(userService.getTotalSpent(user));

        dto.setBookings(userService.getBookingHistory(user));

        model.addAttribute("dto", dto);

        return "admin/user-detail";
    }

    @GetMapping("/hosts")
    public String showHosts(@RequestParam(defaultValue = "0") int page, Model model) {

        Page<User> hostPage = userService.getAllHosts(PageRequest.of(page, 5));

        List<HostDTO> hostDtos = new ArrayList<>();

        for (User host : hostPage.getContent()) {

            HostDTO dto = new HostDTO();

            dto.setUser(host);

            dto.setRevenue(userService.getRevenue(host));

            dto.setTotalHouse(userService.countHouse(host));

            hostDtos.add(dto);
        }

        model.addAttribute("hosts", hostPage);
        model.addAttribute("hostDtos", hostDtos);

        return "admin/hosts";
    }

    @GetMapping("/hosts/{id}")
    public String hostDetail(@PathVariable Long id, Model model) {

        User host = userService.findById(id).orElseThrow(() -> new RuntimeException("Host không tồn tại"));

        HostDetailDTO dto = new HostDetailDTO();

        dto.setHost(host);

        dto.setRevenue(userService.getRevenue(host));

        dto.setHouses(userService.getHostHouses(host));

        model.addAttribute("dto", dto);

        return "admin/host-detail";

    }

    @PostMapping("/users/{id}/lock")
    public String lockUser(@PathVariable Long id) {

        userService.lockUser(id);

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable Long id) {

        userService.unlockUser(id);

        return "redirect:/admin/users";
    }

    @PostMapping("/hosts/{id}/approve")
    public String approveHost(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirectAttributes) {

        if (reason == null || reason.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do duyệt.");

            return "redirect:/admin/hosts";
        }

        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        userService.approveHost(id);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {

            emailService.sendHostApprovalEmail(user.getEmail(), user.getFullName(), reason);
        }

        if (notificationService != null) {

            notificationService.sendNotification(user, "Đăng ký chủ nhà được duyệt", "Yêu cầu đăng ký chủ nhà của bạn đã được duyệt. " + "Lý do: " + reason);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đăng ký chủ nhà và gửi thông báo.");

        return "redirect:/admin/hosts";
    }

    @PostMapping("/hosts/{id}/reject")
    public String rejectHost(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirectAttributes) {

        if (reason == null || reason.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập lý do từ chối.");

            return "redirect:/admin/hosts";
        }

        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        userService.rejectHost(id);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {

            emailService.sendHostRejectionEmail(user.getEmail(), user.getFullName(), reason);
        }

        if (notificationService != null) {

            notificationService.sendNotification(user, "Đăng ký chủ nhà bị từ chối", "Yêu cầu đăng ký chủ nhà của bạn bị từ chối. " + "Lý do: " + reason);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đăng ký và gửi thông báo.");

        return "redirect:/admin/hosts";
    }

    @PostMapping("/hosts/{id}/lock")
    public String lockHost(@PathVariable Long id) {

        User user = userService.findById(id).orElseThrow();

        userService.lockUser(id);

        notificationService.sendNotification(user, "Tài khoản bị khóa", "Tài khoản chủ nhà của bạn đã bị Quản trị viên khóa.");

        return "redirect:/admin/hosts";
    }

    @PostMapping("/hosts/{id}/unlock")
    public String unlockHost(@PathVariable Long id) {

        User user = userService.findById(id).orElseThrow();

        userService.unlockUser(id);

        notificationService.sendNotification(user, "Tài khoản được mở khóa", "Quản trị viên đã mở khóa tài khoản của bạn.");

        return "redirect:/admin/hosts";
    }
}
