package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.User;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"house", "renter"})
    Page<Booking> findByRenter(User renter, Pageable pageable);

    List<Booking> findByRenter(User renter);

    @EntityGraph(attributePaths = {"house", "renter"})
    Page<Booking> findByHouse_Host(User host, Pageable pageable);

    long countByHouse_Host(User host);

    long countByRenter(User renter);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.house.id = :houseId
            AND b.status = vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED
            AND (:startDate < b.endDate AND :endDate > b.startDate)
            """)
    boolean existsOverlappingBooking(@Param("houseId") Long houseId,
                                     @Param("startDate") java.time.LocalDate startDate,
                                     @Param("endDate") java.time.LocalDate endDate);

    @Query("""
            SELECT COALESCE(SUM(b.totalPrice),0)
            FROM Booking b
            WHERE b.house.host = :host
            AND b.status = vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED
            """)
    Double getRevenue(@Param("host") User host);

    @Query("""
            SELECT COALESCE(SUM(b.totalPrice),0)
            FROM Booking b
            WHERE b.renter = :renter
            AND b.status = vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED
            """)
    Double getTotalSpent(@Param("renter") User renter);

    @EntityGraph(attributePaths = {"house", "renter"})
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.house.host = :host
        AND (:houseName IS NULL OR LOWER(b.house.name) LIKE LOWER(CONCAT('%', :houseName, '%')))
        AND (:startDate IS NULL OR b.endDate >= :startDate)
        AND (:endDate IS NULL OR b.startDate <= :endDate)
        AND (:status IS NULL OR b.status = :status)
        """)
    Page<Booking> searchHostBookings(
            @Param("host") User host,
            @Param("houseName") String houseName,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("status") Booking.BookingStatus status,
            Pageable pageable
    );
}
