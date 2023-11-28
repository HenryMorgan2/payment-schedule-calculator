package com.github.henrymorgan2.paymentschedulecalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@Component
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestDTO {

    private String user_email;
    private BigDecimal initialPrincipalAmount;
    private BigDecimal interestRate;
    private BigDecimal term;
    private BigDecimal paymentDay;

}
