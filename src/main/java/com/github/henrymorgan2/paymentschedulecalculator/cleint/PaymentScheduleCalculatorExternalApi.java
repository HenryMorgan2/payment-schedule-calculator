package com.github.henrymorgan2.paymentschedulecalculator.cleint;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "getNonWorkingDays")
public interface PaymentScheduleCalculatorExternalApi {

    @GetMapping
    PaymentScheduleCalculatorCleintResponceDTO getNonWorkingCalendar(@RequestParam(value = "start") String start,
                                                                     @RequestParam(value = "end") String end);
}
