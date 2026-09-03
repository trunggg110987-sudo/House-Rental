package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

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
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User renter;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;


    public enum BookingStatus {
        PENDING,       // Chờ duyệt
        APPROVED,      // Đã phê duyệt - chờ nhận phòng
        CHECKED_IN,    // Đang ở
        CHECKED_OUT,   // Đã trả phòng
        REJECTED,      // Đã từ chối
        CANCELLED      // Đã hủy
    }

    public String getStatusVietnamese() {
        if (status == null) return "Không xác định";
        switch (status) {
            case PENDING: return "Chờ duyệt";
            case APPROVED: return "Đã phê duyệt";
            case CHECKED_IN: return "Đang ở";
            case CHECKED_OUT: return "Đã trả phòng";
            case REJECTED: return "Đã từ chối";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
}
