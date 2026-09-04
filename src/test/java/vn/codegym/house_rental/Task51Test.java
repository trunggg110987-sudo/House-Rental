package vn.codegym.house_rental;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.codegym.house_rental.dto.ChatMessageDTO;
import vn.codegym.house_rental.model.*;
import vn.codegym.house_rental.repository.*;
import vn.codegym.house_rental.service.ChatService;

import java.util.List;

@SpringBootTest
@Transactional
class Task51Test {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testTask51_SendChatMessageAndRetrieveConversation() {
        // 1. Setup Host, Guest, House
        User host = userRepository.save(User.builder()
                .username("host_task51")
                .password("123456")
                .fullName("Lê Văn Chủ Nhà")
                .email("host51@example.com")
                .phone("0912345678")
                .role(User.Role.ROLE_HOST)
                .active(true)
                .build());

        User guest = userRepository.save(User.builder()
                .username("guest_task51")
                .password("123456")
                .fullName("Hoàng Thị Khách")
                .email("guest51@example.com")
                .phone("0987654321")
                .role(User.Role.ROLE_USER)
                .active(true)
                .build());

        Category category = categoryRepository.save(Category.builder().name("Nhà phố Task 51").build());
        House house = houseRepository.save(House.builder()
                .name("Villa ven hồ Task 51")
                .address("123 Phố Cổ")
                .category(category)
                .host(host)
                .pricePerDay(1000000.0)
                .pricePerMonth(25000000.0)
                .numberOfBedrooms(3)
                .numberOfBathrooms(2)
                .status(House.HouseStatus.AVAILABLE)
                .build());

        // 2. Khách gửi tin nhắn cho chủ nhà (Task 51: Người dùng chat với chủ nhà trước khi thuê)
        ChatMessageDTO sentMsg1 = chatService.sendMessage(
                house.getId(),
                guest.getId(),
                host.getId(),
                "Xin chào chủ nhà, nhà mình còn phòng vào cuối tuần này không ạ?"
        );

        Assertions.assertNotNull(sentMsg1.getId());
        Assertions.assertEquals(house.getId(), sentMsg1.getHouseId());
        Assertions.assertEquals(guest.getId(), sentMsg1.getSenderId());
        Assertions.assertEquals(host.getId(), sentMsg1.getReceiverId());
        Assertions.assertEquals("Xin chào chủ nhà, nhà mình còn phòng vào cuối tuần này không ạ?", sentMsg1.getContent());
        Assertions.assertTrue(sentMsg1.getIsMine());
        Assertions.assertFalse(sentMsg1.getIsRead());

        // Kiểm tra danh sách đối tác chat của chủ nhà (Host thấy khách đã nhắn tin)
        List<vn.codegym.house_rental.dto.ChatPartnerDTO> hostPartners = chatService.getHouseChatPartners(house.getId(), host.getId());
        Assertions.assertEquals(1, hostPartners.size());
        Assertions.assertEquals(guest.getId(), hostPartners.get(0).getUserId());
        Assertions.assertEquals("Hoàng Thị Khách", hostPartners.get(0).getFullName());
        Assertions.assertEquals(1, hostPartners.get(0).getUnreadCount());
        Assertions.assertEquals(1, chatService.getHouseUnreadCount(house.getId(), host.getId()));

        // 3. Chủ nhà trả lời tin nhắn
        ChatMessageDTO sentMsg2 = chatService.sendMessage(
                house.getId(),
                host.getId(),
                guest.getId(),
                "Chào bạn, nhà vẫn còn trống cuối tuần này nhé!"
        );

        // 4. Lấy lịch sử hội thoại từ góc nhìn của khách
        List<ChatMessageDTO> guestConversation = chatService.getConversation(house.getId(), guest.getId(), host.getId());
        Assertions.assertEquals(2, guestConversation.size());
        Assertions.assertTrue(guestConversation.get(0).getIsMine()); // Tin nhắn 1 do khách gửi
        Assertions.assertFalse(guestConversation.get(1).getIsMine()); // Tin nhắn 2 do chủ nhà gửi

        // 5. Kiểm tra số tin nhắn chưa đọc của chủ nhà
        long hostUnread = chatService.getUnreadCount(house.getId(), host.getId(), guest.getId());
        Assertions.assertEquals(1, hostUnread);

        // 6. Đánh dấu đã đọc
        chatService.markAsRead(house.getId(), host.getId(), guest.getId());
        long hostUnreadAfter = chatService.getUnreadCount(house.getId(), host.getId(), guest.getId());
        Assertions.assertEquals(0, hostUnreadAfter);

        // 7. Thoát trang và quay lại: Cuộc trò chuyện vẫn được lưu đầy đủ trong database
        List<ChatMessage> persistedInDb = chatMessageRepository.findConversation(house.getId(), guest.getId(), host.getId());
        Assertions.assertEquals(2, persistedInDb.size());
        Assertions.assertEquals("Xin chào chủ nhà, nhà mình còn phòng vào cuối tuần này không ạ?", persistedInDb.get(0).getContent());
        Assertions.assertEquals("Chào bạn, nhà vẫn còn trống cuối tuần này nhé!", persistedInDb.get(1).getContent());
    }

    @Test
    void testTask51_ValidationEmptyAndSelfMessage() {
        User user = userRepository.save(User.builder()
                .username("single_user_51")
                .password("123456")
                .fullName("Single User")
                .email("single51@example.com")
                .phone("0900000000")
                .role(User.Role.ROLE_USER)
                .active(true)
                .build());

        Category category = categoryRepository.save(Category.builder().name("Chung cư").build());
        House house = houseRepository.save(House.builder()
                .name("Chung cư Mini")
                .address("999 Cầu Giấy")
                .category(category)
                .host(user)
                .pricePerDay(500000.0)
                .pricePerMonth(12000000.0)
                .numberOfBedrooms(1)
                .numberOfBathrooms(1)
                .status(House.HouseStatus.AVAILABLE)
                .build());

        // Gửi nội dung rỗng -> lỗi
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(house.getId(), user.getId(), user.getId(), "   ");
        });

        // Tự gửi cho chính mình -> lỗi
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(house.getId(), user.getId(), user.getId(), "Hello myself");
        });
    }
}
