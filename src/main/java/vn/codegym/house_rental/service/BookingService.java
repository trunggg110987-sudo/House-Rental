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
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
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
        booking.setHouse(house);
        booking.setRenter(renter);
        booking.setStatus(Booking.BookingStatus.PENDING);

        // Tính tổng số tiền thuê dựa theo số ngày hoặc số tháng
        long days = ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate());
        if (days <= 0) {
            days = 1;
        }
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
                // Đánh dấu nhà đã được thuê
                booking.getHouse().setStatus(House.HouseStatus.RENTED);
            } else if (status == Booking.BookingStatus.REJECTED || status == Booking.BookingStatus.CANCELLED) {
                // Trả về trạng thái có sẵn nếu hủy/từ chối
                booking.getHouse().setStatus(House.HouseStatus.AVAILABLE);
            }
            return bookingRepository.save(booking);
        }
        throw new RuntimeException("Không tìm thấy đơn đặt nhà ID: " + bookingId);
    }
}
