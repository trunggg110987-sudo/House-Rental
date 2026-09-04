package vn.codegym.house_rental.dto;

import lombok.*;
import java.time.format.DateTimeFormatter;
import vn.codegym.house_rental.model.ChatMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long houseId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String content;
    private String createdAt;
    private Boolean isRead;
    private Boolean isMine;

    public static ChatMessageDTO fromEntity(ChatMessage msg, Long currentUserId) {
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .houseId(msg.getHouse() != null ? msg.getHouse().getId() : null)
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderName(msg.getSender() != null ? msg.getSender().getFullName() : "")
                .receiverId(msg.getReceiver() != null ? msg.getReceiver().getId() : null)
                .receiverName(msg.getReceiver() != null ? msg.getReceiver().getFullName() : "")
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt() != null ? msg.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")) : "")
                .isRead(msg.getIsRead())
                .isMine(msg.getSender() != null && msg.getSender().getId().equals(currentUserId))
                .build();
    }
}