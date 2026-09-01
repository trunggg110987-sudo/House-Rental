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

import jakarta.servlet.http.HttpSession;

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
    public String createBooking(@RequestParam("houseId") Long houseId,
                                @ModelAttribute("booking") Booking booking,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        // [CẢI TIẾN]: Loại bỏ hardcode "user1", lấy tài khoản đang đăng nhập từ Session
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<House> houseOptional = houseService.findById(houseId);
        if (houseOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin nhà.");
            return "redirect:/";
        }

        try {
            bookingService.createBooking(houseOptional.get(), currentUser, booking);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu thuê nhà thành công! Vui lòng chờ chủ nhà phê duyệt.");
            return "redirect:/bookings/my-bookings";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/houses/" + houseId;
        }
    }

    // Danh sách đơn thuê của Khách (My Bookings)
    @GetMapping("/my-bookings")
    public String myBookings(@RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "5") int size,
                             HttpSession session,
                             Model model) {

        // [CẢI TIẾN]: Loại bỏ hardcode "user1", lấy tài khoản đang đăng nhập từ Session
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<Booking> bookingPage = bookingService.findByRenter(currentUser, page, size);
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        return "booking/my_bookings";
    }

    // Quản lý yêu cầu thuê phòng dành cho Chủ nhà (Host Dashboard)
    @GetMapping("/host-requests")
    public String hostRequests(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "5") int size,
                               HttpSession session,
                               Model model) {

        // [CẢI TIẾN]: Loại bỏ hardcode "host1", lấy tài khoản Host đang đăng nhập từ Session
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<Booking> bookingPage = bookingService.findByHost(currentUser, page, size);
        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        return "booking/host_requests";
    }

    // Lịch đặt thuê của Chủ nhà
    @GetMapping("/host-bookings")
    public String hostBookings(
            @RequestParam(name = "houseName", required = false) String houseName,
            @RequestParam(name = "startDate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) java.time.LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            ) java.time.LocalDate endDate,
            @RequestParam(name = "status", required = false) Booking.BookingStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        // Nếu nhập sai khoảng ngày
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            model.addAttribute("errorMessage",
                    "Ngày kết thúc không được trước ngày bắt đầu.");

            startDate = null;
            endDate = null;
        }

        // Tránh page âm
        if (page < 0) {
            page = 0;
        }

        Page<Booking> bookingPage = bookingService.searchHostBookings(
                currentUser,
                houseName,
                startDate,
                endDate,
                status,
                page,
                size
        );

        model.addAttribute("bookingPage", bookingPage);

        model.addAttribute("houseName", houseName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);

        return "booking/host_bookings";
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

    // [CẢI TIẾN]: Bổ sung endpoint cho phép Người thuê tự hủy đơn đặt phòng khi đang PENDING
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            bookingService.cancelBooking(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn đặt nhà thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings/my-bookings";
    }
    @PostMapping("/{id}/checkin")
    public String checkinBooking(@PathVariable("id") Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            bookingService.updateStatus(id, Booking.BookingStatus.CHECKED_IN);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xác nhận khách nhận phòng."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/bookings/host-bookings";
    }


    @PostMapping("/{id}/checkout")
    public String checkoutBooking(@PathVariable("id") Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            bookingService.updateStatus(id, Booking.BookingStatus.CHECKED_OUT);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã xác nhận khách trả phòng."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/bookings/host-bookings";
    }
    
}
