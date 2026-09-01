package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.HouseImage;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseImageRepository extends JpaRepository<HouseImage, Long> {

    List<HouseImage> findByHouse(House house);

    void deleteByHouse(House house);

    Optional<HouseImage> findByIdAndHouse(Long id, House house);
}
