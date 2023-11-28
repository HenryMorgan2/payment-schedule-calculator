package com.github.henrymorgan2.paymentschedulecalculator.controllers;

import com.github.henrymorgan2.paymentschedulecalculator.cleint.PaymentScheduleCalculatorExternalApi;
import com.github.henrymorgan2.paymentschedulecalculator.cleint.PaymentScheduleCalculatorCleintResponceDTO;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import com.github.henrymorgan2.paymentschedulecalculator.service.PaymentScheduleCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentScheduleCalculatorController {

    private final PaymentScheduleCalculatorService paymentScheduleCalculatorService;

    @PostMapping("/calculate-payment-schedule")
    public String getAPaymentSchedule(@RequestBody() RequestDTO requestDTO) {

        List<HashMap> paymentSchedule = paymentScheduleCalculatorService.getPaymentSchedule(requestDTO);

        return "OK";
//        LocalDate.now().plusMonths(term+1); //- окончание периода
//        LocalDate.now() ///- начало периода
//        //LocalDate вынести в отдельную переменную
//        //Вместо double использовать BigDecimal




    }

}
