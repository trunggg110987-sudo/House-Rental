package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.codegym.house_rental.dto.MonthlyIncomeDTO;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {


    // =========================================================
    // BOOKING CỦA RENTER
    // =========================================================

    @EntityGraph(attributePaths = {"house", "renter"})
    Page<Booking> findByRenter(
            User renter,
            Pageable pageable
    );

    List<Booking> findByRenter(User renter);


    // =========================================================
    // BOOKING CỦA HOST
    // =========================================================

    @EntityGraph(attributePaths = {"house", "renter"})
    Page<Booking> findByHouse_Host(
            User host,
            Pageable pageable
    );


    long countByHouse_Host(User host);

    long countByRenter(User renter);


    // =========================================================
    // KIỂM TRA TRÙNG LỊCH KHI KHÁCH TẠO BOOKING
    //
    // PENDING
    // APPROVED
    // CHECKED_IN
    //
    // đều khóa lịch.
    // =========================================================

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.house.id = :houseId

        AND b.status IN (
            vn.codegym.house_rental.model.Booking.BookingStatus.PENDING,
            vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED,
            vn.codegym.house_rental.model.Booking.BookingStatus.CHECKED_IN
        )

        AND (
            :startDate < b.endDate
            AND :endDate > b.startDate
        )
    """)
    boolean existsOverlappingBookingIncludingPending(
            @Param("houseId") Long houseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    // =========================================================
    // KIỂM TRA TRÙNG LỊCH KHI HOST DUYỆT
    //
    // PENDING không khóa ở bước này vì booking hiện tại
    // cũng đang PENDING.
    //
    // APPROVED + CHECKED_IN mới được kiểm tra.
    // =========================================================

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.house.id = :houseId

        AND b.status IN (
            vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED,
            vn.codegym.house_rental.model.Booking.BookingStatus.CHECKED_IN
        )

        AND (
            :startDate < b.endDate
            AND :endDate > b.startDate
        )
    """)
    boolean existsOverlappingBooking(
            @Param("houseId") Long houseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    // =========================================================
    // DOANH THU HOST
    //
    // CANCELLED KHÔNG ĐƯỢC TÍNH.
    // REJECTED KHÔNG ĐƯỢC TÍNH.
    // PENDING KHÔNG ĐƯỢC TÍNH.
    // =========================================================

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0)
        FROM Booking b
        WHERE b.house.host = :host

        AND b.status IN (
            vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED,
            vn.codegym.house_rental.model.Booking.BookingStatus.CHECKED_IN,
            vn.codegym.house_rental.model.Booking.BookingStatus.CHECKED_OUT
        )
    """)
    Double getRevenue(
            @Param("host") User host
    );


    // =========================================================
    // TỔNG TIỀN RENTER ĐÃ CHI
    //
    // CANCELLED / REJECTED / PENDING không tính.
    // =========================================================

    @Query("""
    SELECT COALESCE(SUM(b.totalPrice), 0)
    FROM Booking b
    WHERE b.renter = :renter
      AND b.status IN (
          vn.codegym.house_rental.model.Booking$BookingStatus.APPROVED,
          vn.codegym.house_rental.model.Booking$BookingStatus.CHECKED_IN,
          vn.codegym.house_rental.model.Booking$BookingStatus.CHECKED_OUT
      )
""")
    Double getTotalSpent(User renter);


    // =========================================================
    // DOANH THU THEO THÁNG
    // =========================================================

    @Query("""
        SELECT new vn.codegym.house_rental.dto.MonthlyIncomeDTO(
            MONTH(b.endDate),
            COALESCE(SUM(b.totalPrice), 0.0),
            COUNT(b)
        )
        FROM Booking b
        WHERE b.house.host.id = :hostId

        AND b.status IN (
            vn.codegym.house_rental.model.Booking.BookingStatus.APPROVED,
            vn.codegym.house_rental.model.Booking.BookingStatus.CHECKED_IN,
            vn.codegym.house_rental.model.Booking.BookingStatus.CHECKED_OUT
        )

        AND YEAR(b.endDate) = :year

        GROUP BY MONTH(b.endDate)
        ORDER BY MONTH(b.endDate)
    """)
    List<MonthlyIncomeDTO> getMonthlyIncomeByHostAndYear(
            @Param("hostId") Long hostId,
            @Param("year") int year
    );


    // =========================================================
    // TÌM KIẾM BOOKING CỦA HOST
    // =========================================================

    @EntityGraph(attributePaths = {"house", "renter"})
    @Query("""
        SELECT b
        FROM Booking b

        WHERE b.house.host = :host

        AND (
            :houseName IS NULL
            OR :houseName = ''
            OR LOWER(b.house.name)
                LIKE LOWER(CONCAT('%', :houseName, '%'))
        )

        AND (
            :startDate IS NULL
            OR b.endDate >= :startDate
        )

        AND (
            :endDate IS NULL
            OR b.startDate <= :endDate
        )

        AND (
            :status IS NULL
            OR b.status = :status
        )
    """)
    Page<Booking> searchHostBookings(
            @Param("host") User host,
            @Param("houseName") String houseName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Booking.BookingStatus status,
            Pageable pageable
    );
}