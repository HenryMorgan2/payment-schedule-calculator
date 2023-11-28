package com.github.henrymorgan2.paymentschedulecalculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ResponceDTO {

    private String errorMassage;
    private int errorCode;

}
