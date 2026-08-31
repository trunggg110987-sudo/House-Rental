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

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.codegym.house_rental.model.HouseImage;
import vn.codegym.house_rental.model.HouseStatusPeriod;
import vn.codegym.house_rental.repository.HouseImageRepository;
import vn.codegym.house_rental.repository.HouseRepository;
import vn.codegym.house_rental.repository.HouseStatusPeriodRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HouseService {

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseImageRepository houseImageRepository;

    @Autowired
    private HouseStatusPeriodRepository houseStatusPeriodRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public Page<House> searchHouses(String keyword, Long categoryId, Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return houseRepository.searchHouses((keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null, categoryId, minPrice, maxPrice, pageable);
    }

    public Page<House> searchAvailableHouses(String keyword, Long categoryId, Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return houseRepository.searchAvailableHouses((keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null, categoryId, minPrice, maxPrice, pageable);
    }

    public List<House> getTop5MostRentedHouses() {
        Pageable pageable = PageRequest.of(0, 5);
        List<House> topRented = new ArrayList<>(houseRepository.findTopRentedHouses(pageable));

        // Nếu số lượng nhà có đơn được duyệt chưa đủ 5, bổ sung các nhà sẵn có để trang chủ hiển thị đủ 5 căn
        if (topRented.size() < 5) {
            List<House> availableHouses = houseRepository.findAll(PageRequest.of(0, 5, Sort.by("id").descending())).getContent();
            for (House house : availableHouses) {
                if (!topRented.contains(house) && topRented.size() < 5) {
                    topRented.add(house);
                }
            }
        }
        return topRented;
    }

    public Page<House> findByHost(User host, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return houseRepository.findByHost(host, pageable);
    }

    public Page<House> findByHostAndStatus(User host, House.HouseStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (status == null) {
            return houseRepository.findByHost(host, pageable);
        }
        return houseRepository.findByHostAndStatus(host, status, pageable);
    }

    public Optional<House> findById(Long id) {
        return houseRepository.findById(id);
    }

    public House save(House house) {
        return houseRepository.save(house);
    }

    public void saveHouseImages(House house, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return;
        }

        List<HouseImage> imagesToSave = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            if (file != null && !file.isEmpty()) {
                String uploadedUrl = fileStorageService.storeFile(file);
                if (uploadedUrl != null) {
                    HouseImage image = HouseImage.builder()
                            .imageUrl(uploadedUrl)
                            .house(house)
                            .build();
                    imagesToSave.add(image);
                }
            }
        }

        if (!imagesToSave.isEmpty()) {
            houseImageRepository.saveAll(imagesToSave);
            // Cập nhật thumbnailUrl bằng ảnh đầu tiên nếu thumbnailUrl chưa được đặt hoặc dùng ảnh vừa upload

        }
    }

    public HouseStatusPeriod addStatusPeriod(House house, House.HouseStatus status, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }

        HouseStatusPeriod statusPeriod = HouseStatusPeriod.builder()
                .house(house)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Cập nhật trạng thái nhà nếu khoảng thời gian bao gồm ngày hiện tại
        LocalDate today = LocalDate.now();
        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            house.setStatus(status);
            houseRepository.save(house);
        }

        return houseStatusPeriodRepository.save(statusPeriod);
    }

    public List<HouseStatusPeriod> getStatusPeriods(House house) {
        return houseStatusPeriodRepository.findByHouse(house);
    }

    public void deleteById(Long id) {
        houseRepository.deleteById(id);
    }
}
