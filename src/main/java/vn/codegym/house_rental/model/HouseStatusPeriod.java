package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "house_status_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseStatusPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private House.HouseStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;
}