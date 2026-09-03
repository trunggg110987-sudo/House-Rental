package vn.codegym.house_rental.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.service.NotificationService;

@ControllerAdvice
public class NotificationControllerAdvice {

    @Autowired
    private NotificationService notificationService;

    @ModelAttribute("unreadNotificationCount")
    public long addUnreadNotificationCount(HttpSession session) {
        if (session == null) {
            return 0;
        }
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null && currentUser.getId() != null) {
            return notificationService.countUnread(currentUser);
        }
        return 0;
    }
}