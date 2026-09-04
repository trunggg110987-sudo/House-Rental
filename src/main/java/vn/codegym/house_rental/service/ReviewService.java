package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.BookingRepository;
import vn.codegym.house_rental.repository.ReviewRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<Review> getReviewsByHost(Long hostId) {
        return reviewRepository.findReviewsByHostId(hostId);
    }

    public Review rateBooking(Long bookingId, Integer rating, String comment, User renter) {
        if (renter == null || renter.getId() == null) {
            throw new IllegalStateException("Vui lòng đăng nhập để đánh giá.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Đánh giá phải từ 1 đến 5 sao.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn đặt phòng."));

        if (booking.getRenter() == null || !booking.getRenter().getId().equals(renter.getId())) {
            throw new IllegalStateException("Bạn không có quyền đánh giá đơn thuê này.");
        }

        if (booking.getStatus() != Booking.BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Chỉ được đánh giá với ngôi nhà đã đặt thuê và đơn ở trạng thái 'Đã trả phòng'.");
        }

        // Kiểm tra xem đơn thuê này đã có đánh giá chưa
        Optional<Review> existingOpt = reviewRepository.findByBookingId(bookingId);
        Review review;
        if (existingOpt.isPresent()) {
            review = existingOpt.get();
            review.setRating(rating);
        } else {
            review = Review.builder()
                    .booking(booking)
                    .house(booking.getHouse())
                    .renter(renter)
                    .rating(rating)
                    .createdAt(LocalDateTime.now())
                    .hidden(false)
                    .build();
        }

        // Bổ sung phần nhận xét giới hạn trong 100 từ (Task 45)
        if (comment != null && !comment.trim().isEmpty()) {
            String trimmedComment = comment.trim();
            String[] words = trimmedComment.split("\\s+");
            if (words.length > 100) {
                throw new IllegalArgumentException("Nhận xét không được vượt quá 100 từ (hiện tại: " + words.length + " từ).");
            }
            review.setComment(trimmedComment);
        }

        return reviewRepository.save(review);
    }

    public Review rateBooking(Long bookingId, Integer rating, User renter) {
        return rateBooking(bookingId, rating, null, renter);
    }

    public Map<Long, Review> getReviewsMapByRenter(User renter) {
        Map<Long, Review> map = new HashMap<>();
        if (renter == null || renter.getId() == null) {
            return map;
        }
        List<Review> reviews = reviewRepository.findByRenter(renter);
        for (Review r : reviews) {
            if (r.getBooking() != null && r.getBooking().getId() != null) {
                map.put(r.getBooking().getId(), r);
            }
        }
        return map;
    }

    public void hideReview(Long reviewId, User host) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhận xét."));

        if (review.getHouse() == null || review.getHouse().getHost() == null
                || !review.getHouse().getHost().getId().equals(host.getId())) {
            throw new IllegalStateException("Bạn không có quyền ẩn nhận xét này.");
        }

        review.setHidden(true);
        reviewRepository.save(review);
    }

    public void unhideReview(Long reviewId, User host) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhận xét."));

        if (review.getHouse() == null || review.getHouse().getHost() == null
                || !review.getHouse().getHost().getId().equals(host.getId())) {
            throw new IllegalStateException("Bạn không có quyền thao tác trên nhận xét này.");
        }

        review.setHidden(false);
        reviewRepository.save(review);
    }

    public double getAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (Review r : reviews) {
            if (Boolean.TRUE.equals(r.getHidden())) {
                continue;
            }
            if (r.getRating() != null) {
                sum += r.getRating();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return Math.round((sum / count) * 10.0) / 10.0;
    }

    public Map<Integer, Integer> getStarDistribution(List<Review> reviews) {
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }
        if (reviews != null) {
            for (Review r : reviews) {
                if (Boolean.TRUE.equals(r.getHidden())) {
                    continue;
                }
                if (r.getRating() != null) {
                    int rating = r.getRating();
                    if (rating >= 1 && rating <= 5) {
                        distribution.put(rating, distribution.get(rating) + 1);
                    }
                }
            }
        }
        return distribution;
    }

    public Page<Review> getVisibleReviewsByHouse(Long houseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findVisibleReviewsByHouseId(houseId, pageable);
    }

    public List<Review> getAllVisibleReviewsByHouse(Long houseId) {
        return reviewRepository.findVisibleReviewsByHouseId(houseId);
    }

    public Review addComment(Long houseId, User renter, String comment) {
        if (renter == null || renter.getId() == null) {
            throw new IllegalStateException("Vui lòng đăng nhập để gửi nhận xét.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung nhận xét không được để trống.");
        }

        // Kiểm tra xem user có đơn thuê nhà này ở trạng thái CHECKED_OUT không
        List<Booking> checkedOutBookings = bookingRepository.findByHouseIdAndRenterIdAndStatusOrderByEndDateDesc(
                houseId, renter.getId(), Booking.BookingStatus.CHECKED_OUT
        );
        if (checkedOutBookings.isEmpty()) {
            throw new IllegalStateException("Chỉ được nhận xét với ngôi nhà đã đặt thuê và đơn ở trạng thái 'Đã trả phòng'.");
        }

        Booking latestBooking = checkedOutBookings.get(0);
        House house = latestBooking.getHouse();

        // Tìm review đã có của renter cho booking này hoặc cho căn nhà này
        Optional<Review> existingReviewOpt = reviewRepository.findByBookingId(latestBooking.getId());
        if (existingReviewOpt.isEmpty()) {
            existingReviewOpt = reviewRepository.findFirstByHouseIdAndRenterId(houseId, renter.getId());
        }

        Review review;
        if (existingReviewOpt.isPresent()) {
            review = existingReviewOpt.get();
            review.setComment(comment.trim());
            review.setCreatedAt(LocalDateTime.now());
            review.setHidden(false);
        } else {
            review = Review.builder()
                    .house(house)
                    .renter(renter)
                    .booking(latestBooking)
                    .comment(comment.trim())
                    .rating(5)
                    .createdAt(LocalDateTime.now())
                    .hidden(false)
                    .build();
        }

        return reviewRepository.save(review);
    }
}
