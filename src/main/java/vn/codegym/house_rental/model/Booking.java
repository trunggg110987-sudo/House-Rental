package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ngày bắt đầu thuê không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc thuê không được để trống")
    private LocalDate endDate;

    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User renter;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

    public enum BookingStatus {
        PENDING,   // Chờ chủ nhà duyệt
        APPROVED,  // Đã được chấp nhận
        REJECTED,  // Đã bị từ chối
        CANCELLED, // Người thuê hủy
        CHECKED_IN, // Khách đã đến nhận phòng.
        CHECKED_OUT // Khách đã trả phòng thành công.
    }

    public String getStatusVietnamese() {
        if (status == null) return "Không xác định";
        switch (status) {
            case PENDING: return "Chờ duyệt";
            case APPROVED: return "Đã phê duyệt";
            case REJECTED: return "Đã từ chối";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
}
