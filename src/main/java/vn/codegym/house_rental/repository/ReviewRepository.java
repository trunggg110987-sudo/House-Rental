package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.house.host.id = :hostId ORDER BY r.createdAt DESC")
    List<Review> findReviewsByHostId(@Param("hostId") Long hostId);

    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findByRenter(User renter);

    Optional<Review> findFirstByHouseIdAndRenterId(Long houseId, Long renterId);

    @Query("SELECT r FROM Review r WHERE r.house.id = :houseId AND (r.hidden IS NULL OR r.hidden = false) ORDER BY r.createdAt DESC")
    Page<Review> findVisibleReviewsByHouseId(@Param("houseId") Long houseId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.house.id = :houseId AND (r.hidden IS NULL OR r.hidden = false) ORDER BY r.createdAt DESC")
    List<Review> findVisibleReviewsByHouseId(@Param("houseId") Long houseId);
}
