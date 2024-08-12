package com.github.henrymorgan2.paymentschedulecalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@AllArgsConstructor
@Component
@Data

public class EntryPaymentShedule implements Serializable {

    private LocalDate workingDayOfPayment;
    private BigDecimal monthlyPaymentAmount;
    private BigDecimal interestAmountPerMonth;
    private BigDecimal loanBody;
    private BigDecimal balanceOwed;

}
