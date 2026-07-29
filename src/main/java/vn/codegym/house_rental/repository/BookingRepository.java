package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByRenter(User renter, Pageable pageable);
    Page<Booking> findByHouse_Host(User host, Pageable pageable);
    Page<Booking> findByHouse(House house, Pageable pageable);
}
