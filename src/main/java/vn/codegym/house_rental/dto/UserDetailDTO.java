package vn.codegym.house_rental.dto;

import lombok.Getter;
import lombok.Setter;
import vn.codegym.house_rental.model.Booking;
import vn.codegym.house_rental.model.User;

import java.util.List;

@Getter
@Setter
public class UserDetailDTO {

    private User user;

    private Double totalSpent;

    private List<Booking> bookings;

}