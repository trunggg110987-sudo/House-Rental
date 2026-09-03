package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.User;
import vn.codegym.house_rental.model.HouseImage;
import vn.codegym.house_rental.model.HouseStatusPeriod;

import vn.codegym.house_rental.repository.HouseRepository;
import vn.codegym.house_rental.repository.HouseImageRepository;
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


    // =========================================================
    // TÌM KIẾM NHÀ
    // =========================================================

    public Page<House> searchHouses(
            String keyword,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        String cleanKeyword =
                (keyword != null && !keyword.trim().isEmpty())
                        ? keyword.trim()
                        : null;

        return houseRepository.searchHouses(
                cleanKeyword,
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );
    }


    // =========================================================
    // TÌM NHÀ CÒN TRỐNG
    // =========================================================

    public Page<House> searchAvailableHouses(
            String keyword,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        String cleanKeyword =
                (keyword != null && !keyword.trim().isEmpty())
                        ? keyword.trim()
                        : null;

        return houseRepository.searchAvailableHouses(
                cleanKeyword,
                categoryId,
                minPrice,
                maxPrice,
                pageable
        );
    }


    // =========================================================
    // TOP 5 NHÀ ĐƯỢC THUÊ NHIỀU
    // =========================================================

    public List<House> getTop5MostRentedHouses() {

        Pageable pageable =
                PageRequest.of(0, 5);

        List<House> topRented =
                new ArrayList<>(
                        houseRepository.findTopRentedHouses(pageable)
                );

        if (topRented.size() < 5) {

            List<House> availableHouses =
                    houseRepository
                            .findAll(
                                    PageRequest.of(
                                            0,
                                            5,
                                            Sort.by("id").descending()
                                    )
                            )
                            .getContent();

            for (House house : availableHouses) {

                if (!topRented.contains(house)
                        && topRented.size() < 5) {

                    topRented.add(house);
                }
            }
        }

        return topRented;
    }


    // =========================================================
    // NHÀ CỦA HOST
    // =========================================================

    public Page<House> findByHost(
            User host,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        return houseRepository.findByHost(
                host,
                pageable
        );
    }


    // =========================================================
    // NHÀ CỦA HOST + TRẠNG THÁI
    // =========================================================

    public Page<House> findByHostAndStatus(
            User host,
            House.HouseStatus status,
            int page,
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("id").descending()
                );

        if (status == null) {

            return houseRepository.findByHost(
                    host,
                    pageable
            );
        }

        return houseRepository.findByHostAndStatus(
                host,
                status,
                pageable
        );
    }


    // =========================================================
    // TÌM NHÀ THEO ID
    // =========================================================

    public Optional<House> findById(Long id) {

        return houseRepository.findById(id);
    }


    // =========================================================
    // LƯU NHÀ
    // =========================================================

    public House save(House house) {

        return houseRepository.save(house);
    }


    // =========================================================
    // LƯU ẢNH NHÀ
    // =========================================================

    public void saveHouseImages(
            House house,
            List<MultipartFile> imageFiles) {

        if (imageFiles == null
                || imageFiles.isEmpty()) {

            return;
        }

        List<HouseImage> imagesToSave =
                new ArrayList<>();

        for (MultipartFile file : imageFiles) {

            if (file != null
                    && !file.isEmpty()) {

                String uploadedUrl =
                        fileStorageService.storeFile(file);

                if (uploadedUrl != null) {

                    HouseImage image =
                            HouseImage.builder()
                                    .imageUrl(uploadedUrl)
                                    .house(house)
                                    .build();

                    imagesToSave.add(image);
                }
            }
        }

        if (!imagesToSave.isEmpty()) {

            houseImageRepository.saveAll(
                    imagesToSave
            );
        }
    }


    // =========================================================
    // XÓA ẢNH NHÀ
    // =========================================================

    public void deleteHouseImage(
            Long imageId,
            House house) {

        Optional<HouseImage> imageOptional =
                houseImageRepository.findByIdAndHouse(
                        imageId,
                        house
                );

        if (imageOptional.isPresent()) {

            houseImageRepository.delete(
                    imageOptional.get()
            );
        }
    }


    // =========================================================
    // THÊM TRẠNG THÁI THEO GIAI ĐOẠN
    // =========================================================
    //
    // MAINTENANCE:
    //
    // 1. Không nhập ngày:
    //    startDate = null
    //    endDate   = null
    //
    //    => Bảo trì không thời hạn
    //
    // 2. Có nhập ngày:
    //    => Bảo trì theo khoảng thời gian
    //
    // AVAILABLE / RENTED:
    //
    //    => Bắt buộc phải có ngày
    //
    // =========================================================

    public HouseStatusPeriod addStatusPeriod(
            House house,
            House.HouseStatus status,
            LocalDate startDate,
            LocalDate endDate) {

        // =====================================================
        // KIỂM TRA NHÀ
        // =====================================================

        if (house == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy căn nhà."
            );
        }


        // =====================================================
        // KIỂM TRA STATUS
        // =====================================================

        if (status == null) {

            throw new IllegalArgumentException(
                    "Trạng thái không được để trống."
            );
        }


        // =====================================================
        // MAINTENANCE KHÔNG THỜI HẠN
        // =====================================================

        if (status == House.HouseStatus.MAINTENANCE
                && startDate == null
                && endDate == null) {

            // Nhà chuyển sang bảo trì ngay
            house.setStatus(
                    House.HouseStatus.MAINTENANCE
            );

            houseRepository.save(house);


            // Tạo period không có ngày
            HouseStatusPeriod period =
                    HouseStatusPeriod.builder()
                            .house(house)
                            .status(
                                    House.HouseStatus.MAINTENANCE
                            )
                            .startDate(null)
                            .endDate(null)
                            .build();

            return houseStatusPeriodRepository.save(
                    period
            );
        }


        // =====================================================
        // CHỈ NHẬP 1 TRONG 2 NGÀY
        // =====================================================

        if (startDate == null
                || endDate == null) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập đầy đủ Từ ngày và Đến ngày."
            );
        }


        // =====================================================
        // KIỂM TRA NGÀY
        // =====================================================

        if (!endDate.isAfter(startDate)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc phải sau ngày bắt đầu."
            );
        }


        // =====================================================
        // KIỂM TRA TRÙNG GIAI ĐOẠN
        // =====================================================

        List<HouseStatusPeriod> existingPeriods =
                houseStatusPeriodRepository.findByHouse(
                        house
                );

        for (HouseStatusPeriod existingPeriod
                : existingPeriods) {

            /*
             * Period bảo trì không thời hạn:
             *
             * startDate = null
             * endDate   = null
             *
             * Không đưa vào phép so sánh ngày.
             */
            if (existingPeriod.getStartDate() != null
                    && existingPeriod.getEndDate() != null
                    && startDate.isBefore(
                    existingPeriod.getEndDate()
            )
                    && endDate.isAfter(
                    existingPeriod.getStartDate()
            )) {

                throw new IllegalArgumentException(
                        "Khoảng thời gian này đã có lịch trạng thái khác."
                );
            }
        }


        // =====================================================
        // TẠO PERIOD
        // =====================================================

        HouseStatusPeriod period =
                HouseStatusPeriod.builder()
                        .house(house)
                        .status(status)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build();


        // =====================================================
        // NẾU PERIOD ĐANG CÓ HIỆU LỰC HÔM NAY
        // =====================================================

        LocalDate today =
                LocalDate.now();

        if (!today.isBefore(startDate)
                && !today.isAfter(endDate)) {

            house.setStatus(status);

            houseRepository.save(house);
        }


        // =====================================================
        // LƯU PERIOD
        // =====================================================

        return houseStatusPeriodRepository.save(
                period
        );
    }


    // =========================================================
    // LẤY DANH SÁCH TRẠNG THÁI THEO NHÀ
    // =========================================================

    public List<HouseStatusPeriod> getStatusPeriods(
            House house) {

        return houseStatusPeriodRepository.findByHouse(
                house
        );
    }


    // =========================================================
    // XÓA NHÀ
    // =========================================================

    public void deleteById(Long id) {

        houseRepository.deleteById(id);
    }
}