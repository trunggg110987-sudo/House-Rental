package vn.codegym.house_rental.dto;

import lombok.Getter;
import lombok.Setter;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;

import java.util.List;

@Getter
@Setter
public class HostDetailDTO {

    private User host;

    private Double revenue;

    private List<House> houses;

}