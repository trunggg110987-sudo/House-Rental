package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.repository.HouseRepository;
import java.util.Optional;

@Service
public class HouseService {

    @Autowired
    private HouseRepository houseRepository;

    public Page<House> searchHouses(String keyword, Long categoryId, Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return houseRepository.searchHouses(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );
    }

    public Page<House> findByHost(User host, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return houseRepository.findByHost(host, pageable);
    }

    public Page<House> findTopByBookingCount(int size) {
        return houseRepository.findTopByBookingCount(PageRequest.of(0, size));
    }

    public Optional<House> findById(Long id) {
        return houseRepository.findById(id);
    }

    public House save(House house) {
        return houseRepository.save(house);
    }

    public void deleteById(Long id) {
        houseRepository.deleteById(id);
    }
}
