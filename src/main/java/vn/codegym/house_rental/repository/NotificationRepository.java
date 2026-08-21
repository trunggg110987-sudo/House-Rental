package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.codegym.house_rental.model.Notification;
import vn.codegym.house_rental.model.User;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

}