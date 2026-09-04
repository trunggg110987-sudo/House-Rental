package vn.codegym.house_rental.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatPartnerDTO {
    private Long userId;
    private String fullName;
    private String username;
    private Long unreadCount;
    private String lastMessage;
    private String lastTime;
}
