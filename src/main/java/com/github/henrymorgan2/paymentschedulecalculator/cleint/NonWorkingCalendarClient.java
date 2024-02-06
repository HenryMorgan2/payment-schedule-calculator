package com.github.henrymorgan2.paymentschedulecalculator.cleint;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "getNonWorkingDays")
public interface NonWorkingCalendarClient {

    @GetMapping
    PaymentScheduleCalculatorCleintResponceDTO getNonWorkingDays(@RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                                 @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end);
}
