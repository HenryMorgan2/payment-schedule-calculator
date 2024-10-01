package com.github.henrymorgan2.paymentschedulecalculator.cleint;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Data
public class PaymentScheduleCalculatorCleintResponceDTO {

    private List<LocalDate> nonWorkingDays;

}
