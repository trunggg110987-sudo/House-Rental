package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.BookingRepository;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

// [CẢI TIẾN]: Bổ sung @Transactional đảm bảo tính toàn vẹn dữ liệu
@Service
@Transactional
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public Page<Booking> findByRenter(User renter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return bookingRepository.findByRenter(renter, pageable);
    }

    public Page<Booking> findByHost(User host, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return bookingRepository.findByHouse_Host(host, pageable);
    }

    public Page<Booking> searchHostBookings(
            User host,
            String houseName,
            LocalDate startDate,
            LocalDate endDate,
            Booking.BookingStatus status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        String keyword = houseName;

        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        if (keyword != null) {
            keyword = keyword.trim();
        }

        return bookingRepository.searchHostBookings(
                host,
                keyword,
                startDate,
                endDate,
                status,
                pageable
        );
    }

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(House house, User renter, Booking booking) {
        // Validate Ngày bắt đầu và Ngày kết thúc hợp lệ
        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu thuê không thể ở trong quá khứ.");
        }
        if (!booking.getEndDate().isAfter(booking.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc thuê phải sau ngày bắt đầu thuê.");
        }

        // Host không được thuê căn nhà do chính mình đăng
        if (house.getHost() != null
                && renter != null
                && house.getHost().getId().equals(renter.getId())) {

            throw new IllegalArgumentException(
                    "Chủ nhà không thể thuê chính căn nhà do mình đăng."
            );
        }

        // Kiểm tra căn nhà có đang ở trạng thái bảo trì không
        if (house.getStatus() == House.HouseStatus.MAINTENANCE) {
            throw new IllegalArgumentException("Căn nhà đang trong thời gian bảo trì, hiện tại không thể đặt phòng.");
        }

        // Kiểm tra lịch bảo trì theo khoảng thời gian
        if (house.getStatusPeriods() != null) {
            for (vn.codegym.house_rental.model.HouseStatusPeriod period : house.getStatusPeriods()) {
                if (period.getStatus() == House.HouseStatus.MAINTENANCE) {
                    if (booking.getStartDate().isBefore(period.getEndDate()) && booking.getEndDate().isAfter(period.getStartDate())) {
                        throw new IllegalArgumentException("Căn nhà có lịch bảo trì từ ngày " + period.getStartDate() + " đến ngày " + period.getEndDate() + ".");
                    }
                }
            }
        }

        // Kiểm tra trùng lịch đã duyệt trong khoảng thời gian này
        boolean isOverlapped = bookingRepository.existsOverlappingBooking(
                house.getId(), booking.getStartDate(), booking.getEndDate()
        );
        if (isOverlapped) {
            throw new IllegalArgumentException("Căn nhà này đã được đặt trong khoảng thời gian bạn chọn.");
        }

        booking.setHouse(house);
        booking.setRenter(renter);
        booking.setStatus(Booking.BookingStatus.PENDING);

        long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        if (days <= 0) days = 1;
        double dailyRate = house.getDisplayPricePerDay();
        double totalPrice = dailyRate * days;
        booking.setTotalPrice(Math.round(totalPrice * 100.0) / 100.0);

        return bookingRepository.save(booking);
    }

    public Booking updateStatus(Long bookingId, Booking.BookingStatus status) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setStatus(status);
            bookingRepository.save(booking);

            // Cập nhật trạng thái nhà nếu thích hợp nhưng không xóa bỏ trạng thái khác nếu có đơn approved khác
            House house = booking.getHouse();
            if (status == Booking.BookingStatus.APPROVED) {
                house.setStatus(House.HouseStatus.RENTED);
            } else if (status == Booking.BookingStatus.REJECTED || status == Booking.BookingStatus.CANCELLED) {
                boolean hasOtherApproved = bookingRepository.existsOverlappingBooking(house.getId(), LocalDate.now(), LocalDate.now().plusYears(10));
                if (!hasOtherApproved && house.getStatus() != House.HouseStatus.MAINTENANCE) {
                    house.setStatus(House.HouseStatus.AVAILABLE);
                }
            }
            return booking;
        }
        throw new RuntimeException("Không tìm thấy đơn đặt nhà ID: " + bookingId);
    }

    // Quy tắc Hủy đơn trước 1 ngày (Allow cancellation for PENDING or APPROVED at least 1 day before startDate)
    public void cancelBooking(Long bookingId, User renter) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt nhà"));

        if (!booking.getRenter().getId().equals(renter.getId())) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn đặt nhà này");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED || booking.getStatus() == Booking.BookingStatus.REJECTED) {
            throw new IllegalStateException("Đơn đặt nhà này đã ở trạng thái bị hủy hoặc bị từ chối");
        }

        // Logic kiểm tra 1 ngày trước startDate
        LocalDate today = LocalDate.now();
        if (booking.getStatus() == Booking.BookingStatus.APPROVED) {
            if (!today.isBefore(booking.getStartDate())) {
                throw new IllegalStateException("Bạn chỉ có thể hủy đơn thuê nhà trước ngày bắt đầu thuê (ngày nhận phòng) tối thiểu 1 ngày.");
            }
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        House house = booking.getHouse();
        boolean hasOtherApproved = bookingRepository.existsOverlappingBooking(house.getId(), LocalDate.now(), LocalDate.now().plusYears(10));
        if (!hasOtherApproved && house.getStatus() != House.HouseStatus.MAINTENANCE) {
            house.setStatus(House.HouseStatus.AVAILABLE);
        }
    }
}
