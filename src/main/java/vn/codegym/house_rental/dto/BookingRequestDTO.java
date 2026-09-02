package vn.codegym.house_rental.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDTO {
    private Long hostId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}
