package vn.codegym.house_rental.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.codegym.house_rental.dto.MonthlyIncomeDTO;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.BookingService;
import vn.codegym.house_rental.service.HouseService;
import vn.codegym.house_rental.service.ReviewService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private ReviewService reviewService;


    // =========================================================
    // TẠO BOOKING - RENTER
    // =========================================================

    @PostMapping("/create")
    public String createBooking(
            @RequestParam("houseId") Long houseId,
            @ModelAttribute("booking") Booking booking,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        Optional<House> houseOptional =
                houseService.findById(houseId);

        if (houseOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không tìm thấy căn nhà."
            );

            return "redirect:/";
        }


        House house =
                houseOptional.get();


        try {

            bookingService.createBooking(
                    house,
                    currentUser,
                    booking
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Gửi yêu cầu thuê nhà thành công! Vui lòng chờ chủ nhà phê duyệt."
            );

            return "redirect:/bookings/my-bookings";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/houses/" + houseId;
        }
    }


    // =========================================================
    // MY BOOKINGS - RENTER
    // =========================================================

    @GetMapping("/my-bookings")
    public String myBookings(
            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            ) int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "5"
            ) int size,

            HttpSession session,
            Model model) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        Page<Booking> bookingPage =
                bookingService.findByRenter(
                        currentUser,
                        page,
                        size
                );


        model.addAttribute(
                "bookings",
                bookingPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "totalPages",
                bookingPage.getTotalPages()
        );

        Map<Long, Review> bookingReviews =
                reviewService.getReviewsMapByRenter(currentUser);

        model.addAttribute(
                "bookingReviews",
                bookingReviews
        );

        return "booking/my_bookings";
    }


    // =========================================================
    // HOST REQUESTS
    // =========================================================

    @GetMapping("/host-requests")
    public String hostRequests(
            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            ) int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "5"
            ) int size,

            HttpSession session,
            Model model) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        Page<Booking> bookingPage =
                bookingService.findByHost(
                        currentUser,
                        page,
                        size
                );


        model.addAttribute(
                "bookings",
                bookingPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "totalPages",
                bookingPage.getTotalPages()
        );


        return "booking/host_requests";
    }


    // =========================================================
    // HOST APPROVE
    // PENDING -> APPROVED
    // =========================================================

    @PostMapping("/{id}/approve")
    public String approveBooking(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        try {

            bookingService.approveBooking(
                    id,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã phê duyệt yêu cầu đặt nhà!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/bookings/host-requests";
    }


    // =========================================================
    // HOST REJECT
    // PENDING -> REJECTED
    // =========================================================

    @PostMapping("/{id}/reject")
    public String rejectBooking(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        try {

            bookingService.rejectBooking(
                    id,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "infoMessage",
                    "Đã từ chối yêu cầu đặt nhà!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/bookings/host-requests";
    }


    // =========================================================
// RENTER HỦY BOOKING
// =========================================================

    @PostMapping("/{id}/cancel")
    public String cancelBooking(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {

            bookingService.cancelBooking(
                    id,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã hủy đơn đặt nhà thành công."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/bookings/my-bookings";
    }


    // =========================================================
    // DOANH THU
    // =========================================================

    @GetMapping("/income-statistics")
    public String incomeStatistics(
            @RequestParam(
                    name = "year",
                    required = false
            ) Integer year,

            HttpSession session,
            Model model) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        if (year == null) {
            year = LocalDate.now().getYear();
        }


        List<MonthlyIncomeDTO> dto =
                bookingService.getReviewMonthlyIncome(
                        currentUser.getId(),
                        year
                );


        model.addAttribute(
                "monthlyIncomes",
                dto
        );

        model.addAttribute(
                "selectedYear",
                year
        );


        return "booking/income_statistics";
    }


    // =========================================================
    // HOST BOOKINGS
    // =========================================================

    @GetMapping("/host-bookings")
    public String hostBookings(

            @RequestParam(
                    name = "houseName",
                    required = false
            )
            String houseName,

            @RequestParam(
                    name = "startDate",
                    required = false
            )
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam(
                    name = "endDate",
                    required = false
            )
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            )
            LocalDate endDate,

            @RequestParam(
                    name = "status",
                    required = false
            )
            Booking.BookingStatus status,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "5"
            )
            int size,

            HttpSession session,
            Model model) {


        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        Page<Booking> bookingPage =
                bookingService.searchHostBookings(
                        currentUser,
                        houseName,
                        startDate,
                        endDate,
                        status,
                        page,
                        size
                );


        model.addAttribute(
                "bookingPage",
                bookingPage
        );

        model.addAttribute(
                "houseName",
                houseName
        );

        model.addAttribute(
                "startDate",
                startDate
        );

        model.addAttribute(
                "endDate",
                endDate
        );

        model.addAttribute(
                "status",
                status
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "totalPages",
                bookingPage.getTotalPages()
        );


        return "booking/host_bookings";
    }


    // =========================================================
    // CHECK-IN
    // APPROVED -> CHECKED_IN
    // =========================================================

    @PostMapping("/{bookingId}/checkin")
    public String checkIn(
            @PathVariable("bookingId") Long bookingId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        try {

            bookingService.checkIn(
                    bookingId,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Check-in thành công."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/bookings/host-bookings";
    }


    // =========================================================
    // CHECK-OUT
    // CHECKED_IN -> CHECKED_OUT
    // =========================================================

    @PostMapping("/{bookingId}/checkout")
    public String checkOut(
            @PathVariable("bookingId") Long bookingId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        try {

            bookingService.checkOut(
                    bookingId,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Check-out thành công."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/bookings/host-bookings";
    }


    // =========================================================
    // HOST REVIEWS
    // =========================================================

    @GetMapping("/host-reviews")
    public String hostReviews(
            HttpSession session,
            Model model) {

        User currentUser =
                (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }


        List<Review> reviews =
                reviewService.getReviewsByHost(
                        currentUser.getId()
                );


        double avgRating =
                reviewService.getAverageRating(
                        reviews
                );


        Map<Integer, Integer> starDist =
                reviewService.getStarDistribution(
                        reviews
                );


        model.addAttribute(
                "reviews",
                reviews
        );

        model.addAttribute(
                "averageRating",
                avgRating
        );

        model.addAttribute(
                "starDistribution",
                starDist
        );

        model.addAttribute(
                "totalReviews",
                reviews.size()
        );


        return "booking/host_reviews";
    }

    // =========================================================
    // HOST ẨN NHẬN XÉT (Task 44)
    // =========================================================
    @PostMapping("/reviews/{id}/hide")
    public String hideReview(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            reviewService.hideReview(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn nhận xét thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/bookings/host-reviews";
    }

    // =========================================================
    // HOST BỎ ẨN NHẬN XÉT
    // =========================================================
    @PostMapping("/reviews/{id}/unhide")
    public String unhideReview(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            reviewService.unhideReview(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã bỏ ẩn nhận xét thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/bookings/host-reviews";
    }

    // =========================================================
    // RENTER ĐÁNH GIÁ NHÀ (Task 45)
    // =========================================================
    @PostMapping("/{id}/rate")
    public String rateBooking(
            @PathVariable("id") Long bookingId,
            @RequestParam("rating") Integer rating,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            reviewService.rateBooking(bookingId, rating, currentUser);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đánh giá căn nhà thành công! Cảm ơn bạn đã đánh giá dịch vụ."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/bookings/my-bookings";
    }
}