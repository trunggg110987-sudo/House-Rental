package vn.codegym.house_rental.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegym.house_rental.model.House;
import vn.codegym.house_rental.model.HouseStatusPeriod;
import vn.codegym.house_rental.repository.HouseStatusPeriodRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class HouseStatusPeriodService {

    @Autowired
    private HouseStatusPeriodRepository repository;

    /**
     * Cập nhật trạng thái theo giai đoạn.
     *
     * MAINTENANCE:
     * - Có thể không nhập ngày.
     * - startDate = null và endDate = null
     *   => bảo trì không thời hạn.
     *
     * AVAILABLE / RENTED:
     * - Bắt buộc phải có startDate và endDate.
     */
    public HouseStatusPeriod saveStatusPeriod(
            House house,
            House.HouseStatus status,
            LocalDate startDate,
            LocalDate endDate) {

        // =====================================================
        // KIỂM TRA HOUSE
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

            HouseStatusPeriod period =
                    new HouseStatusPeriod();

            period.setHouse(house);
            period.setStatus(
                    House.HouseStatus.MAINTENANCE
            );

            // Cho phép NULL
            period.setStartDate(null);
            period.setEndDate(null);

            // Chuyển nhà sang bảo trì ngay
            house.setStatus(
                    House.HouseStatus.MAINTENANCE
            );

            return repository.save(period);
        }

        // =====================================================
        // NẾU CHỈ NHẬP MỘT NGÀY
        // =====================================================


        // =====================================================
        // KIỂM TRA NGÀY
        // =====================================================

        if (!endDate.isAfter(startDate)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc phải sau ngày bắt đầu."
            );
        }

        // =====================================================
        // KIỂM TRA TRÙNG LỊCH
        // =====================================================

        List<HouseStatusPeriod> periods =
                repository.findByHouse(house);

        for (HouseStatusPeriod period : periods) {

            /*
             * Bỏ qua bảo trì không thời hạn
             * vì period này không có ngày.
             */
            if (period.getStartDate() == null
                    || period.getEndDate() == null) {

                continue;
            }

            /*
             * Kiểm tra hai khoảng thời gian có giao nhau.
             */
            if (startDate.isBefore(period.getEndDate())
                    && endDate.isAfter(period.getStartDate())) {

                throw new IllegalArgumentException(
                        "Khoảng thời gian này đã có lịch trạng thái khác."
                );
            }
        }

        // =====================================================
        // TẠO STATUS PERIOD
        // =====================================================

        HouseStatusPeriod newPeriod =
                new HouseStatusPeriod();

        newPeriod.setHouse(house);
        newPeriod.setStatus(status);
        newPeriod.setStartDate(startDate);
        newPeriod.setEndDate(endDate);

        // =====================================================
        // NẾU GIAI ĐOẠN ĐANG DIỄN RA
        // => CẬP NHẬT TRẠNG THÁI HOUSE
        // =====================================================

        LocalDate today = LocalDate.now();

        if (!today.isBefore(startDate)
                && !today.isAfter(endDate)) {

            house.setStatus(status);
        }

        // =====================================================
        // LƯU
        // =====================================================

        return repository.save(newPeriod);
    }

    // =====================================================
    // LẤY DANH SÁCH STATUS PERIOD
    // =====================================================

    public List<HouseStatusPeriod> findByHouse(
            House house) {

        return repository.findByHouse(house);
    }

    // =====================================================
    // XÓA STATUS PERIOD
    // =====================================================

    public void delete(Long id) {
        repository.deleteById(id);
    }
}