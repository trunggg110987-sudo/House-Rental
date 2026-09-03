package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.BookingRepository;
import vn.codegym.house_rental.repository.ReviewRepository;

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

    public Review rateBooking(Long bookingId, Integer rating, User renter) {
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

        return reviewRepository.save(review);
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
        for (Review r : reviews) {
            sum += r.getRating();
        }
        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }

    public Map<Integer, Integer> getStarDistribution(List<Review> reviews) {
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }
        if (reviews != null) {
            for (Review r : reviews) {
                int rating = r.getRating();
                if (rating >= 1 && rating <= 5) {
                    distribution.put(rating, distribution.get(rating) + 1);
                }
            }
        }
        return distribution;
    }
}
