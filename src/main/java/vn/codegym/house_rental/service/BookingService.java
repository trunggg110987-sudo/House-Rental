package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.codegym.house_rental.dto.MonthlyIncomeDTO;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.BookingRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;


    // =========================================================
    // TẠO BOOKING
    // =========================================================

    public Booking createBooking(
            House house,
            User renter,
            Booking booking) {

        if (house == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy căn nhà."
            );
        }

        if (renter == null) {
            throw new IllegalArgumentException(
                    "Người thuê không hợp lệ."
            );
        }

        if (booking == null) {
            throw new IllegalArgumentException(
                    "Thông tin đặt nhà không hợp lệ."
            );
        }

        LocalDate startDate = booking.getStartDate();
        LocalDate endDate = booking.getEndDate();

        // Kiểm tra ngày
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu và ngày kết thúc không được để trống."
            );
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu thuê không thể ở trong quá khứ."
            );
        }

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException(
                    "Ngày kết thúc thuê phải sau ngày bắt đầu thuê."
            );
        }

        // Không cho host thuê nhà của chính mình
        if (house.getHost() != null
                && house.getHost().getId() != null
                && renter.getId() != null
                && house.getHost().getId().equals(renter.getId())) {

            throw new IllegalArgumentException(
                    "Chủ nhà không thể thuê chính căn nhà do mình đăng."
            );
        }

        // Kiểm tra trạng thái nhà
        if (house.getStatus() == House.HouseStatus.MAINTENANCE) {
            throw new IllegalArgumentException(
                    "Căn nhà đang bảo trì, hiện tại không thể đặt."
            );
        }

        // Kiểm tra lịch bảo trì
        if (house.getStatusPeriods() != null) {

            for (var period : house.getStatusPeriods()) {

                if (period.getStatus() == House.HouseStatus.MAINTENANCE
                        && period.getStartDate() != null
                        && period.getEndDate() != null) {

                    boolean overlap =
                            startDate.isBefore(period.getEndDate())
                                    && endDate.isAfter(period.getStartDate());

                    if (overlap) {
                        throw new IllegalArgumentException(
                                "Căn nhà có lịch bảo trì từ "
                                        + period.getStartDate()
                                        + " đến "
                                        + period.getEndDate()
                                        + "."
                        );
                    }
                }
            }
        }

        // =====================================================
        // KIỂM TRA BOOKING TRÙNG LỊCH
        //
        // PENDING
        // APPROVED
        // CHECKED_IN
        // đều khóa lịch
        // =====================================================

        boolean overlapped =
                bookingRepository
                        .existsOverlappingBookingIncludingPending(
                                house.getId(),
                                startDate,
                                endDate
                        );

        if (overlapped) {
            throw new IllegalArgumentException(
                    "Căn nhà này đã có người đặt trong khoảng thời gian bạn chọn."
            );
        }

        // Gán dữ liệu
        booking.setHouse(house);
        booking.setRenter(renter);
        booking.setStatus(
                Booking.BookingStatus.PENDING
        );

        // =====================================================
        // TÍNH TIỀN
        // =====================================================

        long days =
                ChronoUnit.DAYS.between(
                        startDate,
                        endDate
                );

        if (days <= 0) {
            days = 1;
        }

        double dailyRate =
                house.getDisplayPricePerDay();

        double totalPrice =
                dailyRate * days;

        totalPrice =
                Math.round(totalPrice * 100.0) / 100.0;

        booking.setTotalPrice(totalPrice);

        return bookingRepository.save(booking);
    }


    // =========================================================
    // BOOKING CỦA RENTER
    // =========================================================

    public Page<Booking> findByRenter(
            User renter,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        return bookingRepository.findByRenter(
                renter,
                pageable
        );
    }


    public List<Booking> findByRenter(User renter) {

        return bookingRepository.findByRenter(renter);
    }


    // =========================================================
    // BOOKING CỦA HOST
    // =========================================================

    public Page<Booking> findByHost(
            User host,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        return bookingRepository.findByHouse_Host(
                host,
                pageable
        );
    }


    // =========================================================
    // TÌM KIẾM BOOKING CỦA HOST
    // =========================================================

    public Page<Booking> searchHostBookings(
            User host,
            String houseName,
            LocalDate startDate,
            LocalDate endDate,
            Booking.BookingStatus status,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        return bookingRepository.searchHostBookings(
                host,
                houseName,
                startDate,
                endDate,
                status,
                pageable
        );
    }


    // =========================================================
    // HOST PHÊ DUYỆT BOOKING
    // PENDING -> APPROVED
    // =========================================================

    public Booking approveBooking(
            Long bookingId,
            User host) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đơn thuê."
                                )
                        );

        // Kiểm tra quyền host
        checkHostPermission(booking, host);

        // Chỉ PENDING mới được duyệt
        if (booking.getStatus()
                != Booking.BookingStatus.PENDING) {

            throw new IllegalStateException(
                    "Chỉ có thể phê duyệt đơn đang ở trạng thái PENDING."
            );
        }

        House house = booking.getHouse();

        // Kiểm tra lại trùng lịch
        // Chỉ APPROVED + CHECKED_IN được xem là đã chiếm lịch
        boolean overlapped =
                bookingRepository.existsOverlappingBooking(
                        house.getId(),
                        booking.getStartDate(),
                        booking.getEndDate()
                );

        if (overlapped) {
            throw new IllegalStateException(
                    "Không thể phê duyệt vì thời gian thuê đã bị trùng với đơn khác."
            );
        }

        booking.setStatus(
                Booking.BookingStatus.APPROVED
        );

        return bookingRepository.save(booking);
    }


    // =========================================================
    // HOST TỪ CHỐI BOOKING
    // PENDING -> REJECTED
    // =========================================================

    public Booking rejectBooking(
            Long bookingId,
            User host) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đơn thuê."
                                )
                        );

        // Kiểm tra quyền host
        checkHostPermission(booking, host);

        // Chỉ PENDING mới được từ chối
        if (booking.getStatus()
                != Booking.BookingStatus.PENDING) {

            throw new IllegalStateException(
                    "Chỉ có thể từ chối đơn đang ở trạng thái PENDING."
            );
        }

        booking.setStatus(
                Booking.BookingStatus.REJECTED
        );

        return bookingRepository.save(booking);
    }


    // =========================================================
// RENTER HỦY BOOKING
// =========================================================

    public Booking cancelBooking(
            Long bookingId,
            User renter) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đơn thuê."
                                )
                        );

        // =====================================================
        // KIỂM TRA ĐÚNG NGƯỜI THUÊ
        // =====================================================

        if (renter == null
                || renter.getId() == null
                || booking.getRenter() == null
                || booking.getRenter().getId() == null
                || !booking.getRenter()
                .getId()
                .equals(renter.getId())) {

            throw new IllegalStateException(
                    "Bạn không có quyền hủy đơn thuê này."
            );
        }

        // =====================================================
        // CHỈ PENDING MỚI ĐƯỢC HỦY
        // =====================================================

        if (booking.getStatus()
                != Booking.BookingStatus.PENDING) {

            throw new IllegalStateException(
                    "Chỉ có thể hủy đơn đang ở trạng thái PENDING."
            );
        }

        // =====================================================
        // KIỂM TRA NGÀY HỦY
        // =====================================================

        LocalDate today = LocalDate.now();

        LocalDate startDate =
                booking.getStartDate();

        if (startDate == null) {

            throw new IllegalStateException(
                    "Đơn thuê chưa có ngày nhận phòng."
            );
        }

        long daysUntilCheckIn =
                ChronoUnit.DAYS.between(
                        today,
                        startDate
                );

        /*
         * Quy tắc:
         *
         * Còn 0 ngày  -> KHÔNG được hủy
         * Còn 1 ngày  -> KHÔNG được hủy
         * Còn 2 ngày  -> ĐƯỢC hủy
         * Còn 3 ngày  -> ĐƯỢC hủy
         */

        if (daysUntilCheckIn < 1) {

            throw new IllegalStateException(
                    "Không thể hủy vì đã đến ngày nhận phòng."
            );
        }

        // =====================================================
        // HỦY BOOKING
        // =====================================================

        booking.setStatus(
                Booking.BookingStatus.CANCELLED
        );

        return bookingRepository.save(booking);
    }


    // =========================================================
    // CHECK-IN
    // APPROVED -> CHECKED_IN
    // =========================================================

    public Booking checkIn(
            Long bookingId,
            User host) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đơn thuê."
                                )
                        );

        checkHostPermission(booking, host);

        if (booking.getStatus()
                != Booking.BookingStatus.APPROVED) {

            throw new IllegalStateException(
                    "Chỉ có thể check-in đơn đã được phê duyệt."
            );
        }

        booking.setStatus(
                Booking.BookingStatus.CHECKED_IN
        );

        House house = booking.getHouse();

        if (house != null) {
            house.setStatus(
                    House.HouseStatus.RENTED
            );
        }

        return bookingRepository.save(booking);
    }


    // =========================================================
    // CHECK-OUT
    // CHECKED_IN -> CHECKED_OUT
    // =========================================================

    public Booking checkOut(
            Long bookingId,
            User host) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy đơn thuê."
                                )
                        );

        checkHostPermission(booking, host);

        if (booking.getStatus()
                != Booking.BookingStatus.CHECKED_IN) {

            throw new IllegalStateException(
                    "Chỉ có thể check-out đơn đang CHECKED_IN."
            );
        }

        booking.setStatus(
                Booking.BookingStatus.CHECKED_OUT
        );

        House house = booking.getHouse();

        if (house != null) {
            house.setStatus(
                    House.HouseStatus.AVAILABLE
            );
        }

        return bookingRepository.save(booking);
    }


    // =========================================================
    // KIỂM TRA QUYỀN HOST
    // =========================================================

    private void checkHostPermission(
            Booking booking,
            User host) {

        if (host == null
                || host.getId() == null
                || booking.getHouse() == null
                || booking.getHouse().getHost() == null
                || booking.getHouse().getHost().getId() == null
                || !booking.getHouse()
                .getHost()
                .getId()
                .equals(host.getId())) {

            throw new IllegalStateException(
                    "Bạn không có quyền thực hiện thao tác này."
            );
        }
    }


    // =========================================================
    // DOANH THU HOST
    // =========================================================

    public Double getRevenue(User host) {

        Double revenue =
                bookingRepository.getRevenue(host);

        return revenue != null
                ? revenue
                : 0.0;
    }


    // =========================================================
    // TỔNG TIỀN RENTER ĐÃ CHI
    // =========================================================

    public Double getTotalSpent(User renter) {

        Double total =
                bookingRepository.getTotalSpent(renter);

        return total != null
                ? total
                : 0.0;
    }


    // =========================================================
    // DOANH THU THEO THÁNG
    // =========================================================

    public List<MonthlyIncomeDTO> getReviewMonthlyIncome(
            Long hostId,
            int year) {

        return bookingRepository
                .getMonthlyIncomeByHostAndYear(
                        hostId,
                        year
                );
    }


    // =========================================================
    // ĐẾM BOOKING HOST
    // =========================================================

    public long countByHost(User host) {

        return bookingRepository.countByHouse_Host(host);
    }


    // =========================================================
    // ĐẾM BOOKING RENTER
    // =========================================================

    public long countByRenter(User renter) {

        return bookingRepository.countByRenter(renter);
    }
}