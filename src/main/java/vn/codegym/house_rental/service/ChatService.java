package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegym.house_rental.dto.ChatMessageDTO;
import vn.codegym.house_rental.dto.ChatPartnerDTO;
import vn.codegym.house_rental.model.ChatMessage;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.ChatMessageRepository;
import vn.codegym.house_rental.repository.HouseRepository;
import vn.codegym.house_rental.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public ChatMessageDTO sendMessage(Long houseId, Long senderId, Long receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống.");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Không thể gửi tin nhắn cho chính mình.");
        }

        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy căn nhà."));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người nhận."));

        ChatMessage message = ChatMessage.builder()
                .house(house)
                .sender(sender)
                .receiver(receiver)
                .content(content.trim())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Gửi thông báo đến người nhận để hiển thị trên quả chuông
        try {
            String snippet = content.trim();
            if (snippet.length() > 60) {
                snippet = snippet.substring(0, 57) + "...";
            }
            notificationService.sendNotification(
                    receiver,
                    "Tin nhắn mới về căn nhà",
                    sender.getFullName() + " đã gửi tin nhắn về " + house.getName() + ": \"" + snippet + "\""
            );
        } catch (Exception ignored) {
        }

        return ChatMessageDTO.fromEntity(saved, senderId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversation(Long houseId, Long currentUserId, Long otherUserId) {
        List<ChatMessage> messages = chatMessageRepository.findConversation(houseId, currentUserId, otherUserId);
        return messages.stream()
                .map(m -> ChatMessageDTO.fromEntity(m, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long houseId, Long currentUserId, Long otherUserId) {
        return chatMessageRepository.countUnreadMessages(houseId, currentUserId, otherUserId);
    }

    @Transactional(readOnly = true)
    public long getHouseUnreadCount(Long houseId, Long currentUserId) {
        return chatMessageRepository.countTotalUnreadForReceiverInHouse(houseId, currentUserId);
    }

    public void markAsRead(Long houseId, Long currentUserId, Long otherUserId) {
        chatMessageRepository.markConversationAsRead(houseId, currentUserId, otherUserId);
    }

    @Transactional(readOnly = true)
    public List<ChatPartnerDTO> getHouseChatPartners(Long houseId, Long currentUserId) {
        List<ChatMessage> messages = chatMessageRepository.findByHouseIdOrderByCreatedAtDesc(houseId);
        Map<Long, User> partnerMap = new LinkedHashMap<>();
        Map<Long, ChatMessage> latestMessageMap = new HashMap<>();

        for (ChatMessage m : messages) {
            User partner = null;
            if (m.getSender() != null && !m.getSender().getId().equals(currentUserId)) {
                partner = m.getSender();
            } else if (m.getReceiver() != null && !m.getReceiver().getId().equals(currentUserId)) {
                partner = m.getReceiver();
            }

            if (partner != null) {
                partnerMap.putIfAbsent(partner.getId(), partner);
                latestMessageMap.putIfAbsent(partner.getId(), m);
            }
        }

        List<ChatPartnerDTO> result = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        for (Map.Entry<Long, User> entry : partnerMap.entrySet()) {
            Long partnerId = entry.getKey();
            User partner = entry.getValue();
            ChatMessage latest = latestMessageMap.get(partnerId);

            long unread = chatMessageRepository.countUnreadMessages(houseId, currentUserId, partnerId);

            result.add(ChatPartnerDTO.builder()
                    .userId(partnerId)
                    .fullName(partner.getFullName())
                    .username(partner.getUsername())
                    .unreadCount(unread)
                    .lastMessage(latest != null ? latest.getContent() : "")
                    .lastTime(latest != null && latest.getCreatedAt() != null ? latest.getCreatedAt().format(dtf) : "")
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<House> getHousesWithChatsForUser(Long userId) {
        List<Long> houseIds = chatMessageRepository.findHouseIdsWithChatsForUser(userId);
        if (houseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return houseRepository.findAllById(houseIds);
    }
}