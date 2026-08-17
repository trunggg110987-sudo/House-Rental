package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @NotNull(message = "Giá thuê không được để trống")
    @Min(value = 0, message = "Giá thuê phải lớn hơn 0")
    private Double pricePerMonth;

    @Min(value = 1, message = "Số phòng ngủ tối thiểu là 1")
    private Integer numberOfBedrooms;

    @Min(value = 1, message = "Số phòng tắm tối thiểu là 1")
    private Integer numberOfBathrooms;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

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
        AVAILABLE,   // Có sẵn cho thuê
        RENTED,      // Đã được thuê
        MAINTENANCE  // Đang bảo trì
    }
}