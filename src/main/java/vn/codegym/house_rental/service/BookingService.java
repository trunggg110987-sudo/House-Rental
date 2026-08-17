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

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(House house, User renter, Booking booking) {
        // [CẢI TIẾN]: Validate Ngày bắt đầu và Ngày kết thúc hợp lệ
        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu thuê không thể ở trong quá khứ.");
        }
        if (!booking.getEndDate().isAfter(booking.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc thuê phải sau ngày bắt đầu thuê.");
        }

        // [CẢI TIẾN]: Kiểm tra xem nhà có bị trùng lịch đã duyệt trong khoảng thời gian này không
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
        double monthlyRate = house.getPricePerMonth() != null ? house.getPricePerMonth() : 0;
        double totalPrice = (monthlyRate / 30.0) * days;
        booking.setTotalPrice(Math.round(totalPrice * 100.0) / 100.0);

        return bookingRepository.save(booking);
    }

    public Booking updateStatus(Long bookingId, Booking.BookingStatus status) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setStatus(status);
            if (status == Booking.BookingStatus.APPROVED) {
                booking.getHouse().setStatus(House.HouseStatus.RENTED);
            } else if (status == Booking.BookingStatus.REJECTED || status == Booking.BookingStatus.CANCELLED) {
                booking.getHouse().setStatus(House.HouseStatus.AVAILABLE);
            }
            return bookingRepository.save(booking);
        }
        throw new RuntimeException("Không tìm thấy đơn đặt nhà ID: " + bookingId);
    }

    // [CẢI TIẾN]: Bổ sung phương thức cho phép Người thuê tự hủy đơn đặt phòng khi đang ở trạng thái PENDING
    public void cancelBooking(Long bookingId, User renter) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt nhà"));

        if (!booking.getRenter().getId().equals(renter.getId())) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn đặt nhà này");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn đặt nhà khi đang ở trạng thái Chờ duyệt");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
