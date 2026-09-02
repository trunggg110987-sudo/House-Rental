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

    public MonthlyIncomeDTO(int month, Double income) {
        this.month = month;
        this.income = income;
    }

    public MonthlyIncomeDTO(Integer month, Double income) {
        this.month = month != null ? month : 0;
        this.income = income;
    }

    public MonthlyIncomeDTO(Object month, Object income) {
        this.month = month instanceof Number ? ((Number) month).intValue() : 0;
        this.income = income instanceof Number ? ((Number) income).doubleValue() : 0.0;
    }
}


