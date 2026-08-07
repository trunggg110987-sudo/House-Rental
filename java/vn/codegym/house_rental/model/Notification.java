package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tiêu đề
    @Column(nullable = false)
    private String title;

    // Nội dung
    @Column(columnDefinition = "TEXT")
    private String content;

    // Thời gian gửi
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Đã đọc hay chưa
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    // Người nhận
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}