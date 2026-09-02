package vn.codegym.house_rental.controller;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;


    // DANH SÁCH BOOKING CỦA RENTER

    public Page<Booking> findByRenter(User renter, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        return bookingRepository.findByRenter(
                renter,
                pageable
        );
    }


    // DANH SÁCH BOOKING CỦA HOST
    public Page<Booking> findByHost(User host, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        return bookingRepository.findByHouse_Host(
                host,
                pageable
        );
    }


    // TÌM KIẾM LỊCH ĐẶT THUÊ CỦA HOST
    // Có:
    // - Tên nhà
    // - Khoảng ngày
    // - Trạng thái
    // - Phân trang
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

        // Chuẩn hóa ô tìm kiếm tên nhà
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


    // TÌM BOOKING THEO ID

    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }


    // TẠO BOOKING
    public Booking createBooking(
            House house,
            User renter,
            Booking booking) {

        // -----------------------------------------------------
        // Kiểm tra ngày thuê
        // -----------------------------------------------------
        if (booking.getStartDate() == null
                || booking.getEndDate() == null) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu và ngày kết thúc không được để trống."
            );
        }

        if (booking.getStartDate().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu thuê không thể ở trong quá khứ."
            );
        }

        if (!booking.getEndDate().isAfter(booking.getStartDate())) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc thuê phải sau ngày bắt đầu thuê."
            );
        }

        // -----------------------------------------------------
        // Host không được thuê nhà của chính mình
        // -----------------------------------------------------
        if (house.getHost() != null
                && renter != null
                && house.getHost().getId().equals(renter.getId())) {

            throw new IllegalArgumentException(
                    "Chủ nhà không thể thuê chính căn nhà do mình đăng."
            );
        }

        // -----------------------------------------------------
        // Kiểm tra trạng thái bảo trì của nhà
        // -----------------------------------------------------
        if (house.getStatus() == House.HouseStatus.MAINTENANCE) {

            throw new IllegalArgumentException(
                    "Căn nhà đang trong thời gian bảo trì, hiện tại không thể đặt phòng."
            );
        }

        // -----------------------------------------------------
        // Kiểm tra lịch bảo trì
        // -----------------------------------------------------
        if (house.getStatusPeriods() != null) {

            for (vn.codegym.house_rental.model.HouseStatusPeriod period
                    : house.getStatusPeriods()) {

                if (period.getStatus()
                        == House.HouseStatus.MAINTENANCE) {

                    if (booking.getStartDate().isBefore(period.getEndDate())
                            && booking.getEndDate().isAfter(period.getStartDate())) {

                        throw new IllegalArgumentException(
                                "Căn nhà có lịch bảo trì từ ngày "
                                        + period.getStartDate()
                                        + " đến ngày "
                                        + period.getEndDate()
                                        + "."
                        );
                    }
                }
            }
        }

        // -----------------------------------------------------
        // Kiểm tra trùng lịch
        // APPROVED / CHECKED_IN sẽ được Repository xử lý
        // -----------------------------------------------------
        boolean isOverlapped =
                bookingRepository.existsOverlappingBooking(
                        house.getId(),
                        booking.getStartDate(),
                        booking.getEndDate()
                );

        if (isOverlapped) {

            throw new IllegalArgumentException(
                    "Căn nhà này đã được đặt trong khoảng thời gian bạn chọn."
            );
        }

        // -----------------------------------------------------
        // Gán thông tin booking
        // -----------------------------------------------------
        booking.setHouse(house);
        booking.setRenter(renter);
        booking.setStatus(
                Booking.BookingStatus.PENDING
        );

        // -----------------------------------------------------
        // Tính tổng tiền
        // -----------------------------------------------------
        long days = ChronoUnit.DAYS.between(
                booking.getStartDate(),
                booking.getEndDate()
        );

        if (days <= 0) {
            days = 1;
        }

        double dailyRate =
                house.getDisplayPricePerDay();

        double totalPrice =
                dailyRate * days;

        booking.setTotalPrice(
                Math.round(totalPrice * 100.0) / 100.0
        );

        return bookingRepository.save(booking);
    }


    // CẬP NHẬT TRẠNG THÁI BOOKING
    // Dùng cho:
    // - APPROVED
    // - REJECTED
    // - CANCELLED
    public Booking updateStatus(
            Long bookingId,
            Booking.BookingStatus status) {

        Optional<Booking> optionalBooking =
                bookingRepository.findById(bookingId);

        if (optionalBooking.isEmpty()) {

            throw new RuntimeException(
                    "Không tìm thấy đơn đặt nhà ID: "
                            + bookingId
            );
        }

        Booking booking = optionalBooking.get();

        booking.setStatus(status);

        bookingRepository.save(booking);

        House house = booking.getHouse();

        // -----------------------------------------------------
        // APPROVED -> Nhà được đánh dấu RENTED
        // -----------------------------------------------------
        if (status == Booking.BookingStatus.APPROVED) {

            house.setStatus(
                    House.HouseStatus.RENTED
            );
        }

        // -----------------------------------------------------
        // REJECTED / CANCELLED
        // Nếu không còn booking phù hợp thì nhà AVAILABLE
        // -----------------------------------------------------
        else if (status == Booking.BookingStatus.REJECTED
                || status == Booking.BookingStatus.CANCELLED) {

            boolean hasOtherApproved =
                    bookingRepository.existsOverlappingBooking(
                            house.getId(),
                            LocalDate.now(),
                            LocalDate.now().plusYears(10)
                    );

            if (!hasOtherApproved
                    && house.getStatus()
                    != House.HouseStatus.MAINTENANCE) {

                house.setStatus(
                        House.HouseStatus.AVAILABLE
                );
            }
        }

        return booking;
    }


    // KHÁCH HỦY BOOKING

    public void cancelBooking(
            Long bookingId,
            User renter) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy đơn đặt nhà"
                                )
                        );

        // -----------------------------------------------------
        // Kiểm tra quyền
        // -----------------------------------------------------
        if (!booking.getRenter().getId()
                .equals(renter.getId())) {

            throw new IllegalStateException(
                    "Bạn không có quyền hủy đơn đặt nhà này"
            );
        }

        // -----------------------------------------------------
        // Không cho hủy đơn đã kết thúc
        // -----------------------------------------------------
        if (booking.getStatus()
                == Booking.BookingStatus.CANCELLED
                || booking.getStatus()
                == Booking.BookingStatus.REJECTED) {

            throw new IllegalStateException(
                    "Đơn đặt nhà này đã ở trạng thái bị hủy hoặc bị từ chối"
            );
        }

        // -----------------------------------------------------
        // Booking APPROVED phải hủy trước ngày bắt đầu
        // -----------------------------------------------------
        LocalDate today = LocalDate.now();

        if (booking.getStatus()
                == Booking.BookingStatus.APPROVED) {

            if (!today.isBefore(
                    booking.getStartDate())) {

                throw new IllegalStateException(
                        "Bạn chỉ có thể hủy đơn thuê nhà trước ngày bắt đầu thuê "
                                + "(ngày nhận phòng) tối thiểu 1 ngày."
                );
            }
        }

        booking.setStatus(
                Booking.BookingStatus.CANCELLED
        );

        bookingRepository.save(booking);

        // -----------------------------------------------------
        // Cập nhật trạng thái nhà
        // -----------------------------------------------------
        House house = booking.getHouse();

        boolean hasOtherApproved =
                bookingRepository.existsOverlappingBooking(
                        house.getId(),
                        LocalDate.now(),
                        LocalDate.now().plusYears(10)
                );

        if (!hasOtherApproved
                && house.getStatus()
                != House.HouseStatus.MAINTENANCE) {

            house.setStatus(
                    House.HouseStatus.AVAILABLE
            );
        }
    }

  // THỐNG KÊ THU NHẬP THEO THÁNG
    public List<MonthlyIncomeDTO> getReviewMonthlyIncome(
            Long hostId,
            int year) {

        List<MonthlyIncomeDTO> result =
                bookingRepository.getMonthlyIncomeByHostAndYear(
                        hostId,
                        year
                );

        // Khởi tạo đủ 12 tháng
        Map<Integer, Double> monthly =
                new HashMap<>();

        for (int i = 1; i <= 12; i++) {
            monthly.put(i, 0.0);
        }

        // Ghi đè những tháng có doanh thu
        for (MonthlyIncomeDTO dto : result) {

            monthly.put(
                    dto.getMonth(),
                    dto.getIncome()
            );
        }

        // Trả về đủ 12 tháng
        List<MonthlyIncomeDTO> finalResult =
                new ArrayList<>();

        for (int i = 1; i <= 12; i++) {

            Double income = monthly.get(i);

            MonthlyIncomeDTO dto =
                    new MonthlyIncomeDTO(
                            i,
                            income
                    );

            finalResult.add(dto);
        }

        return finalResult;
    }


    // CHECK-IN
    // APPROVED -> CHECKED_IN
    public void checkIn(
            Long bookingId,
            User currentHost) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy booking với id : "
                                                + bookingId
                                )
                        );

        // -----------------------------------------------------
        // Kiểm tra host có sở hữu căn nhà không
        // -----------------------------------------------------
        if (!booking.getHouse()
                .getHost()
                .getId()
                .equals(currentHost.getId())) {

            throw new RuntimeException(
                    "Bạn không có quyền thực hiện check-in cho căn nhà này."
            );
        }

        // -----------------------------------------------------
        // Chỉ APPROVED mới được check-in
        // -----------------------------------------------------
        if (booking.getStatus()
                != Booking.BookingStatus.APPROVED) {

            throw new IllegalStateException(
                    "Chỉ có thể check-in đối với những đơn đặt phòng đã được phê duyệt"
            );
        }

        booking.getHouse().setStatus(
                House.HouseStatus.RENTED
        );

        booking.setStatus(
                Booking.BookingStatus.CHECKED_IN
        );

        bookingRepository.save(booking);
    }


    // CHECK-OUT
    // CHECKED_IN -> CHECKED_OUT
    public void checkOut(
            Long bookingId,
            User currentHost) {

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy booking với id : "
                                                + bookingId
                                )
                        );

        // -----------------------------------------------------
        // Kiểm tra quyền host
        // -----------------------------------------------------
        if (!booking.getHouse()
                .getHost()
                .getId()
                .equals(currentHost.getId())) {

            throw new RuntimeException(
                    "Bạn không có quyền thực hiện check-out cho căn nhà này."
            );
        }

        // -----------------------------------------------------
        // Chỉ CHECKED_IN mới được check-out
        // -----------------------------------------------------
        if (booking.getStatus()
                != Booking.BookingStatus.CHECKED_IN) {

            throw new IllegalStateException(
                    "Bạn chưa check in căn nhà này."
            );
        }

        booking.setStatus(
                Booking.BookingStatus.CHECKED_OUT
        );

        booking.getHouse().setStatus(
                House.HouseStatus.AVAILABLE
        );

        bookingRepository.save(booking);
    }
}
