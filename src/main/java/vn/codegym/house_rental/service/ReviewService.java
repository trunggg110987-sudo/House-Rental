package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.repository.ReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsByHost(Long hostId) {
        return reviewRepository.findReviewsByHostId(hostId);
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
