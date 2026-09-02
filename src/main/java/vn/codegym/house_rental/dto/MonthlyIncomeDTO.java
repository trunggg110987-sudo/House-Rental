package vn.codegym.house_rental.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyIncomeDTO {

    private int month;
    private Double income;
    private Long bookingCount;

    public MonthlyIncomeDTO(int month, Double income, Long bookingCount) {
        this.month = month;
        this.income = income;
        this.bookingCount = bookingCount;
    }

    public MonthlyIncomeDTO(Integer month, Double income, Long bookingCount) {
        this.month = month != null ? month : 0;
        this.income = income != null ? income : 0.0;
        this.bookingCount = bookingCount != null ? bookingCount : 0L;
    }

    // Giữ constructor cũ để không làm hỏng những nơi khác đang sử dụng DTO
    public MonthlyIncomeDTO(int month, Double income) {
        this.month = month;
        this.income = income;
        this.bookingCount = 0L;
    }

    public MonthlyIncomeDTO(Integer month, Double income) {
        this.month = month != null ? month : 0;
        this.income = income != null ? income : 0.0;
        this.bookingCount = 0L;
    }

    public MonthlyIncomeDTO(Object month, Object income) {
        this.month = month instanceof Number
                ? ((Number) month).intValue()
                : 0;

        this.income = income instanceof Number
                ? ((Number) income).doubleValue()
                : 0.0;

        this.bookingCount = 0L;
    }

    public MonthlyIncomeDTO(Object month, Object income, Object bookingCount) {
        this.month = month instanceof Number
                ? ((Number) month).intValue()
                : 0;

        this.income = income instanceof Number
                ? ((Number) income).doubleValue()
                : 0.0;

        this.bookingCount = bookingCount instanceof Number
                ? ((Number) bookingCount).longValue()
                : 0L;
    }
}