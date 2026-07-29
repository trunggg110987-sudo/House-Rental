package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
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

    // Gửi yêu cầu đặt phòng (Renter)
    @PostMapping("/create")
    public String createBooking(
            @RequestParam("houseId") Long houseId,
            @ModelAttribute("booking") Booking booking,
            RedirectAttributes redirectAttributes) {

        Optional<House> houseOptional = houseService.findById(houseId);
        Optional<User> renterOptional = userService.findByUsername("user1"); // Renter giả lập

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
            Model model) {

        Optional<User> renterOptional = userService.findByUsername("user1");
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
            Model model) {

        Optional<User> hostOptional = userService.findByUsername("host1");
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
