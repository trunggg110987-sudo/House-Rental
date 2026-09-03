package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "houses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên nhà/phòng không được để trống")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotNull(message = "Giá thuê theo ngày không được để trống")
    @Min(value = 0, message = "Giá thuê phải lớn hơn 0")
    @Max(value = 500000000L, message = "Giá thuê theo ngày tối đa là 500.000.000 VNĐ")
    private Double pricePerDay;

    private Double pricePerMonth;

    @NotNull(message = "Số phòng ngủ không được để trống")
    @Min(value = 1, message = "Số phòng ngủ tối thiểu là 1")
    @Max(value = 50, message = "Số phòng ngủ tối đa là 50")
    private Integer numberOfBedrooms;

    @NotNull(message = "Số phòng tắm không được để trống")
    @Min(value = 1, message = "Số phòng tắm tối thiểu là 1")
    @Max(value = 50, message = "Số phòng tắm tối đa là 50")
    private Integer numberOfBathrooms;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HouseImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "house", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HouseStatusPeriod> statusPeriods = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HouseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User host;

    public enum HouseStatus {
        AVAILABLE,   // Còn trống
        RENTED,      // Đã cho thuê
        MAINTENANCE  // Đang bảo trì
    }

    public String getStatusVietnamese() {
        if (status == null) return "Không xác định";
        switch (status) {
            case AVAILABLE: return "Còn trống";
            case RENTED: return "Đã cho thuê";
            case MAINTENANCE: return "Đang bảo trì";
            default: return status.name();
        }
    }

    public Double getDisplayPricePerDay() {
        if (pricePerDay != null && pricePerDay > 0) {
            return pricePerDay;
        }
        if (pricePerMonth != null && pricePerMonth > 0) {
            return Math.round((pricePerMonth / 30.0) * 100.0) / 100.0;
        }
        return 0.0;
    }
}