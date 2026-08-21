package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.HouseStatusPeriod;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HouseStatusPeriodRepository extends JpaRepository<HouseStatusPeriod, Long> {
    List<HouseStatusPeriod> findByHouse(House house);
    List<HouseStatusPeriod> findByHouseId(Long houseId);

    @Query("SELECT hsp FROM HouseStatusPeriod hsp WHERE hsp.house.id = :houseId AND :date BETWEEN hsp.startDate AND hsp.endDate")
    List<HouseStatusPeriod> findActiveStatusPeriods(@Param("houseId") Long houseId, @Param("date") LocalDate date);
}
