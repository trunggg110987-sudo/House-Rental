package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.Review;
import vn.codegym.house_rental.model.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =========================================================
    // REVIEW THEO HOST - Dùng cho trang Host Reviews
    // =========================================================

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.house.host.id = :hostId
        ORDER BY r.createdAt DESC
    """)
    List<Review> findReviewsByHostId(
            @Param("hostId") Long hostId
    );


    // =========================================================
    // REVIEW THEO TỪNG NHÀ - CARD 43
    // Chỉ hiển thị review chưa bị ẩn
    // =========================================================

    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.renter
        WHERE r.house.id = :houseId
        AND r.hidden = false
        ORDER BY r.createdAt DESC
    """)
    Page<Review> findVisibleReviewsByHouseId(
            @Param("houseId") Long houseId,
            Pageable pageable
    );

    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findByRenter(User renter);

    Optional<Review> findFirstByHouseIdAndRenterId(Long houseId, Long renterId);

    @Query("""
    SELECT COALESCE(AVG(r.rating), 0)
    FROM Review r
    WHERE r.house.id = :houseId
    AND r.hidden = false
""")
    Double getAverageRatingByHouseId(
            @Param("houseId") Long houseId
    );


}