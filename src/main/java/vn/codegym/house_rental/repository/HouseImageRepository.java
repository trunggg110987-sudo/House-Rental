package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.HouseImage;

import java.util.List;

@Repository
public interface HouseImageRepository extends JpaRepository<HouseImage, Long> {
    List<HouseImage> findByHouse(House house);
    void deleteByHouse(House house);
}
