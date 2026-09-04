package vn.codegym.house_rental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.codegym.house_rental.model.*;
import vn.codegym.house_rental.repository.*;
import vn.codegym.house_rental.service.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Transactional
class Task50Test {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void testTask50_HostReceivesNotificationOnReviewAndComment() {
        // Tạo Host
        User host = userRepository.save(User.builder()
                .username("host_test_50")
                .password("123456")
                .fullName("Nguyễn Văn Chủ Nhà")
                .email("host50@example.com")
                .phone("0987111333")
                .role(User.Role.ROLE_HOST)
                .active(true)
                .build());

        // Tạo Renter
        User renter = userRepository.save(User.builder()
                .username("renter_test_50")
                .password("123456")
                .fullName("Trần Thị Khách Thuê")
                .email("renter50@example.com")
                .phone("0987444555")
                .role(User.Role.ROLE_USER)
                .active(true)
                .build());

        // Tạo Category & House
        Category category = categoryRepository.save(Category.builder().name("Căn hộ dịch vụ").build());
        House house = houseRepository.save(House.builder()
                .name("Căn Hộ Panorama Sài Gòn")
                .address("456 Đường Nguyễn Huệ")
                .category(category)
                .host(host)
                .pricePerDay(800000.0)
                .pricePerMonth(20000000.0)
                .numberOfBedrooms(2)
                .numberOfBathrooms(1)
                .status(House.HouseStatus.AVAILABLE)
                .build());

        // Tạo Booking đã trả phòng (CHECKED_OUT)
        Booking booking = bookingRepository.save(Booking.builder()
                .house(house)
                .renter(renter)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(2))
                .status(Booking.BookingStatus.CHECKED_OUT)
                .totalPrice(2400000.0)
                .build());

        long unreadBefore = notificationService.countUnread(host);

        // Khách gửi đánh giá và nhận xét (Task 50)
        reviewService.rateBooking(booking.getId(), 5, "Nhà rất đẹp, sạch sẽ và chủ nhà hỗ trợ rất nhiệt tình!", renter);

        // 1. Kiểm tra số lượng thông báo chưa đọc của chủ nhà tăng thêm 1
        long unreadAfter = notificationService.countUnread(host);
        Assertions.assertEquals(unreadBefore + 1, unreadAfter);

        // 2. Kiểm tra nội dung thông báo
        List<Notification> notifications = notificationService.getNotifications(host);
        Assertions.assertFalse(notifications.isEmpty());

        Notification latest = notifications.get(0);
        Assertions.assertEquals("Khách nhận xét căn nhà", latest.getTitle());
        Assertions.assertTrue(latest.getContent().contains("Trần Thị Khách Thuê đã nhận xét Căn Hộ Panorama Sài Gòn vào ngày"));
        Assertions.assertFalse(latest.getIsRead());
    }
}