package vn.codegym.house_rental.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.codegym.house_rental.dto.ChatMessageDTO;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.ChatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestParam("houseId") Long houseId,
            @RequestParam("otherUserId") Long otherUserId,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để xem tin nhắn."));
        }

        List<ChatMessageDTO> list = chatService.getConversation(houseId, currentUser.getId(), otherUserId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để gửi tin nhắn."));
        }

        try {
            Long houseId = Long.valueOf(payload.get("houseId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            String content = (String) payload.get("content");

            ChatMessageDTO sent = chatService.sendMessage(houseId, currentUser.getId(), receiverId, content);
            return ResponseEntity.ok(sent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            @RequestParam("houseId") Long houseId,
            @RequestParam("otherUserId") Long otherUserId,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.ok(Map.of("unreadCount", 0));
        }

        long count = chatService.getUnreadCount(houseId, currentUser.getId(), otherUserId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markRead(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        }

        try {
            Long houseId = Long.valueOf(payload.get("houseId").toString());
            Long otherUserId = Long.valueOf(payload.get("otherUserId").toString());

            chatService.markAsRead(houseId, currentUser.getId(), otherUserId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/partners")
    public ResponseEntity<?> getPartners(
            @RequestParam("houseId") Long houseId,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        }

        return ResponseEntity.ok(chatService.getHouseChatPartners(houseId, currentUser.getId()));
    }

    @GetMapping("/house-unread")
    public ResponseEntity<?> getHouseUnread(
            @RequestParam("houseId") Long houseId,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.ok(Map.of("unreadCount", 0));
        }

        long count = chatService.getHouseUnreadCount(houseId, currentUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}