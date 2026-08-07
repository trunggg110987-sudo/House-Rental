package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.Notification;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Gửi thông báo
    public void sendNotification(User user,
                                 String title,
                                 String content){

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .user(user)
                .build();

        notificationRepository.save(notification);
    }

    // Lấy danh sách thông báo
    public List<Notification> getNotifications(User user){

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);

    }

}