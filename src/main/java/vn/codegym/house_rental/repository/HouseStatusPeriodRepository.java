package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.HouseStatusPeriod;

import java.util.List;

@Repository
public interface HouseStatusPeriodRepository
        extends JpaRepository<HouseStatusPeriod, Long> {

    List<HouseStatusPeriod> findByHouse(House house);
}