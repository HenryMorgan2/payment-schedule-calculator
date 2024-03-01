package com.github.henrymorgan2.paymentschedulecalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;

@RequiredArgsConstructor
@AllArgsConstructor
@Component
@Data

public class EntryPaymentShedule implements Serializable {

    private String workingDayOfPayment;
    private BigDecimal monthlyPaymentAmount;
    private BigDecimal interestAmountPerMonth;
    private BigDecimal loanBody;
    private BigDecimal balanceOwed;

}
