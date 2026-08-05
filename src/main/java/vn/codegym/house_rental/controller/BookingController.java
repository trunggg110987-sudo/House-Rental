package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.security.CustomUserDetails;
import vn.codegym.house_rental.security.oauth2.CustomOAuth2User;
import vn.codegym.house_rental.service.BookingService;
import vn.codegym.house_rental.service.HouseService;
import vn.codegym.house_rental.service.UserService;

import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getUser());
        } else if (principal instanceof CustomOAuth2User oauth2User) {
            return Optional.of(oauth2User.getUser());
        }
        return Optional.empty();
    }

    // Gửi yêu cầu đặt phòng (Renter)
    @PostMapping("/create")
    public String createBooking(
            @RequestParam("houseId") Long houseId,
            @ModelAttribute("booking") Booking booking,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Optional<House> houseOptional = houseService.findById(houseId);
        Optional<User> renterOptional = getCurrentUser(authentication);
        if (renterOptional.isEmpty()) {
            renterOptional = userService.findByUsername("user1");
        }

        if (houseOptional.isPresent() && renterOptional.isPresent()) {
            bookingService.createBooking(houseOptional.get(), renterOptional.get(), booking);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu thuê nhà thành công! Vui lòng chờ chủ nhà phê duyệt.");
            return "redirect:/bookings/my-bookings";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Không thể hoàn tất gửi yêu cầu đặt nhà.");
        return "redirect:/houses/" + houseId;
    }

    // Danh sách đơn thuê của Khách (My Bookings)
    @GetMapping("/my-bookings")
    public String myBookings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Authentication authentication,
            Model model) {

        Optional<User> renterOptional = getCurrentUser(authentication);
        if (renterOptional.isEmpty()) {
            renterOptional = userService.findByUsername("user1");
        }

        if (renterOptional.isPresent()) {
            Page<Booking> bookingPage = bookingService.findByRenter(renterOptional.get(), page, size);
            model.addAttribute("bookings", bookingPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookingPage.getTotalPages());
        }
        return "booking/my_bookings";
    }

    // Quản lý yêu cầu thuê phòng dành cho Chủ nhà (Host Dashboard)
    @GetMapping("/host-requests")
    public String hostRequests(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Authentication authentication,
            Model model) {

        Optional<User> hostOptional = getCurrentUser(authentication);
        if (hostOptional.isEmpty()) {
            hostOptional = userService.findByUsername("host1");
        }

        if (hostOptional.isPresent()) {
            Page<Booking> bookingPage = bookingService.findByHost(hostOptional.get(), page, size);
            model.addAttribute("bookings", bookingPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookingPage.getTotalPages());
        }
        return "booking/host_requests";
    }

    // Chủ nhà Phê duyệt yêu cầu (PENDING -> APPROVED)
    @PostMapping("/{id}/approve")
    public String approveBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        bookingService.updateStatus(id, Booking.BookingStatus.APPROVED);
        redirectAttributes.addFlashAttribute("successMessage", "Đã phê duyệt yêu cầu đặt nhà!");
        return "redirect:/bookings/host-requests";
    }

    // Chủ nhà Từ chối yêu cầu (PENDING -> REJECTED)
    @PostMapping("/{id}/reject")
    public String rejectBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        bookingService.updateStatus(id, Booking.BookingStatus.REJECTED);
        redirectAttributes.addFlashAttribute("infoMessage", "Đã từ chối yêu cầu đặt nhà!");
        return "redirect:/bookings/host-requests";
    }
}
