package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.Review;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.house.host.id = :hostId ORDER BY r.createdAt DESC")
    List<Review> findReviewsByHostId(@Param("hostId") Long hostId);
}
