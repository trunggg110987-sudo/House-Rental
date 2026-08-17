package vn.codegym.house_rental.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "house_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;
}
