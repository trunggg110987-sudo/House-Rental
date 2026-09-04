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
class Task49Test {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testTask49_HostReceivesNotificationOnBooking() {
        // Tạo Host
        User host = userRepository.save(User.builder()
                .username("host_test_49")
                .password("123456")
                .fullName("Nguyễn Văn Chủ Nhà")
                .email("host49@example.com")
                .phone("0987111222")
                .role(User.Role.ROLE_HOST)
                .active(true)
                .build());

        // Tạo Renter
        User renter = userRepository.save(User.builder()
                .username("renter_test_49")
                .password("123456")
                .fullName("Trần Thị Khách Thuê")
                .email("renter49@example.com")
                .phone("0987333444")
                .role(User.Role.ROLE_USER)
                .active(true)
                .build());

        // Tạo Category & House
        Category category = categoryRepository.save(Category.builder().name("Biệt thự nghỉ dưỡng").build());
        House house = houseRepository.save(House.builder()
                .name("Villa Biển Đà Nẵng")
                .address("100 Đường Võ Nguyên Giáp")
                .category(category)
                .host(host)
                .pricePerDay(1200000.0)
                .pricePerMonth(30000000.0)
                .numberOfBedrooms(3)
                .numberOfBathrooms(2)
                .status(House.HouseStatus.AVAILABLE)
                .build());

        long unreadBefore = notificationService.countUnread(host);

        // Khách đặt thuê nhà
        Booking booking = Booking.builder()
                .house(house)
                .renter(renter)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(6))
                .status(Booking.BookingStatus.PENDING)
                .build();

        bookingService.createBooking(house, renter, booking);

        // 1. Kiểm tra số thông báo chưa đọc của chủ nhà tăng thêm 1
        long unreadAfter = notificationService.countUnread(host);
        Assertions.assertEquals(unreadBefore + 1, unreadAfter);

        // 2. Kiểm tra danh sách thông báo và nội dung thông báo
        List<Notification> notifications = notificationService.getNotifications(host);
        Assertions.assertFalse(notifications.isEmpty());

        Notification latest = notifications.get(0);
        Assertions.assertEquals("Khách đặt thuê nhà", latest.getTitle());
        Assertions.assertTrue(latest.getContent().contains("Trần Thị Khách Thuê đã đặt thuê Villa Biển Đà Nẵng vào ngày"));
        Assertions.assertFalse(latest.getIsRead());
    }
}