package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer rating;
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    private User renter;

    @ManyToOne(fetch = FetchType.LAZY)
    private House house;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private LocalDateTime createdAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean hidden = false;

    public boolean isHidden() {
        return Boolean.TRUE.equals(hidden);
    }
}
