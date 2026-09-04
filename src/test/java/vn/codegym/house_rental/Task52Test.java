package vn.codegym.house_rental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.codegym.house_rental.model.*;
import vn.codegym.house_rental.repository.*;
import vn.codegym.house_rental.service.ChatService;

@SpringBootTest
@Transactional
class Task52Test {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testTask52_ChatNotificationBadgeCountCycle() {
        // 1. Khởi tạo Host, Guest và Căn nhà
        User host = userRepository.save(User.builder()
                .username("host_task52")
                .password("123456")
                .fullName("Nguyễn Văn Host 52")
                .email("host52@example.com")
                .phone("0911222333")
                .role(User.Role.ROLE_HOST)
                .active(true)
                .build());

        User guest = userRepository.save(User.builder()
                .username("guest_task52")
                .password("123456")
                .fullName("Trần Thị Guest 52")
                .email("guest52@example.com")
                .phone("0944555666")
                .role(User.Role.ROLE_USER)
                .active(true)
                .build());

        Category category = categoryRepository.save(Category.builder().name("Căn hộ Task 52").build());
        House house = houseRepository.save(House.builder()
                .name("Căn hộ Landmark 52")
                .address("789 Điện Biên Phủ")
                .category(category)
                .host(host)
                .pricePerDay(1200000.0)
                .pricePerMonth(28000000.0)
                .numberOfBedrooms(2)
                .numberOfBathrooms(2)
                .status(House.HouseStatus.AVAILABLE)
                .build());

        // 2. Ban đầu khi chưa có tin nhắn: số lượng tin nhắn chưa đọc của cả 2 bên = 0
        Assertions.assertEquals(0, chatService.getHouseUnreadCount(house.getId(), host.getId()));
        Assertions.assertEquals(0, chatService.getHouseUnreadCount(house.getId(), guest.getId()));

        // 3. Khách gửi tin nhắn 1 và tin nhắn 2 đến chủ nhà (khi cửa sổ của chủ nhà đang đóng)
        chatService.sendMessage(house.getId(), guest.getId(), host.getId(), "Chào chủ nhà, nhà còn trống không?");
        chatService.sendMessage(house.getId(), guest.getId(), host.getId(), "Mình muốn thuê từ ngày mai.");

        // Yêu cầu: "Khi có tin nhắn đến và đi thì sẽ có thông báo về số lượng tin nhắn ở biểu tượng chat nếu cửa sổ giao tiếp đang đóng"
        // Chủ nhà có 2 tin nhắn chưa đọc
        long hostUnreadCount = chatService.getHouseUnreadCount(house.getId(), host.getId());
        Assertions.assertEquals(2, hostUnreadCount);

        // Khách gửi đi nên số lượng tin chưa đọc của khách vẫn là 0
        long guestUnreadCount = chatService.getHouseUnreadCount(house.getId(), guest.getId());
        Assertions.assertEquals(0, guestUnreadCount);

        // 4. Chủ nhà mở cửa sổ giao tiếp:
        // Yêu cầu: "Khi mở cửa sổ, số lượng tin nhắn sẽ biến mất"
        chatService.markAsRead(house.getId(), host.getId(), guest.getId());
        long hostUnreadAfterOpen = chatService.getHouseUnreadCount(house.getId(), host.getId());
        Assertions.assertEquals(0, hostUnreadAfterOpen);

        // 5. Chủ nhà đóng cửa sổ và gửi tin nhắn phản hồi cho khách
        chatService.sendMessage(house.getId(), host.getId(), guest.getId(), "Chào bạn, nhà vẫn còn trống nhé!");

        // Khách (đang đóng cửa sổ) nhận được tin nhắn đến:
        // Biểu tượng chat của khách hiện badge số lượng tin nhắn = 1
        long guestUnreadAfterHostReply = chatService.getHouseUnreadCount(house.getId(), guest.getId());
        Assertions.assertEquals(1, guestUnreadAfterHostReply);

        // Chủ nhà gửi tin đi nên badge của chủ nhà vẫn = 0
        Assertions.assertEquals(0, chatService.getHouseUnreadCount(house.getId(), host.getId()));

        // 6. Khách mở cửa sổ giao tiếp -> số lượng tin nhắn biến mất
        chatService.markAsRead(house.getId(), guest.getId(), host.getId());
        Assertions.assertEquals(0, chatService.getHouseUnreadCount(house.getId(), guest.getId()));

        // 7. Yêu cầu: "và hiện lại khi đóng và có tin nhắn mới"
        // Chủ nhà gửi thêm tin nhắn mới khi khách đã đóng cửa sổ
        chatService.sendMessage(house.getId(), host.getId(), guest.getId(), "Bạn có cần xem video căn nhà không?");
        long guestUnreadNew = chatService.getHouseUnreadCount(house.getId(), guest.getId());
        Assertions.assertEquals(1, guestUnreadNew);
    }
}
