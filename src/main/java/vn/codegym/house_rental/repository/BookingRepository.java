package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.codegym.house_rental.model.User;

import java.util.List;

import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByRenter(User renter, Pageable pageable);

    Page<Booking> findByHouse_Host(User host, Pageable pageable);

    List<Booking> findByRenter(User Renter);

    // [CẢI TIẾN]: Bổ sung Query kiểm tra xem căn nhà có đơn thuê APPROVED trùng khoảng thời gian chọn hay không
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
                    AND b.status = vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED""")
    Double getRevenue(@Param("host") User host);

    @Query("""
            SELECT COALESCE(SUM(b.totalPrice),0)
                FROM Booking b
                WHERE b.renter = :renter
                AND b.status =
                vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED""")
    Double getTotalSpent(@Param("renter") User renter);
}
