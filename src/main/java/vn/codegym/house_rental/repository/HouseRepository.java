package vn.codegym.house_rental.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {

    Page<House> findByHost(User host, Pageable pageable);

    @Query("SELECT h FROM House h WHERE " +
            "h.status = vn.codegym.house_rental.model.House$HouseStatus.AVAILABLE AND " +
           "(:keyword IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR h.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR h.pricePerMonth >= :minPrice) AND " +
           "(:maxPrice IS NULL OR h.pricePerMonth <= :maxPrice)")
    Page<House> searchHouses(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    @Query("SELECT h FROM House h LEFT JOIN Booking b ON b.house = h " +
           "GROUP BY h ORDER BY COUNT(b.id) DESC, h.id DESC")
    Page<House> findTopByBookingCount(Pageable pageable);
}
