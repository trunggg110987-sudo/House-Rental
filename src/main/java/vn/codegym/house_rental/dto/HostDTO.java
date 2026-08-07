package vn.codegym.house_rental.dto;

import lombok.Getter;
import lombok.Setter;
import vn.codegym.house_rental.model.User;

@Getter
@Setter
public class HostDTO {

    private User user;

    private Double revenue;

    private Long totalHouse;

}