package vn.codegym.house_rental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.codegym.house_rental.dto.MonthlyIncomeDTO;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.BookingService;
import vn.codegym.house_rental.service.HouseService;
import vn.codegym.house_rental.service.UserService;
import java.util.List;

import java.util.Optional;
import java.time.LocalDate;

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

    @GetMapping("/income-statistics")
    public String index(@RequestParam(name = "year", required = false) Integer year, HttpSession session, Model model){
        User currentUser = (User) session.getAttribute("currentUser");
        if(currentUser == null){
            return "redirect:/login";
        }

        if(year == null){
            year = LocalDate.now().getYear();
        }

        List<MonthlyIncomeDTO> dto = bookingService.getReviewMonthlyIncome(currentUser.getId(), year);
        model.addAttribute("monthlyIncomes", dto);
        model.addAttribute("selectedYear" , year);

        return "booking/income_statistics";
    }

    @GetMapping("/host-bookings")
    public String hostBookings(@RequestParam(name = "houseName", required = false) String houseName,
                               @RequestParam(name = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam(name = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
                               @RequestParam(name = "status", required = false) Booking.BookingStatus status,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "5") int size,
                               HttpSession session,
                               Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Page<Booking> bookingPage = bookingService.searchHostBookings(
                currentUser, houseName, startDate, endDate, status, page, size
        );

        model.addAttribute("bookingPage", bookingPage);
        model.addAttribute("houseName", houseName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());

        return "booking/host_bookings";
    }

    @PostMapping("/{bookingId}/checkin")
    public String checkIn(@PathVariable("bookingId") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes){
        User currentUser = (User) session.getAttribute("currentUser");
        if(currentUser == null){
            return "redirect:/login";
        }

        try {
            bookingService.checkIn(bookingId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Check in thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings/host-bookings";
    }

    @PostMapping("/{bookingId}/checkout")
    public String checkOut(@PathVariable("bookingId") Long bookingId, HttpSession session, RedirectAttributes redirectAttributes){
        User currentUser = (User) session.getAttribute("currentUser");
        if(currentUser == null){
            return "redirect:/login";
        }

        try {
            bookingService.checkOut(bookingId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Check out thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings/host-bookings";
    }
}
