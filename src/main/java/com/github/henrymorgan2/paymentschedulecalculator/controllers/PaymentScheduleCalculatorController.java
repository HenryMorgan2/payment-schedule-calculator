package com.github.henrymorgan2.paymentschedulecalculator.controllers;

import com.github.henrymorgan2.paymentschedulecalculator.dto.EntryPaymentShedule;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import com.github.henrymorgan2.paymentschedulecalculator.service.PaymentScheduleCalculatorService;
import com.github.henrymorgan2.paymentschedulecalculator.utils.GenerationPDF;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentScheduleCalculatorController {

    private final PaymentScheduleCalculatorService paymentScheduleCalculatorService;
    private final GenerationPDF generationPDF;

    @PostMapping("/calculate-payment-schedule")
    public String getAPaymentSchedule(@RequestBody() RequestDTO requestDTO) throws DocumentException, IOException {


        List<EntryPaymentShedule> paymentSchedule = paymentScheduleCalculatorService.getPaymentSchedule(requestDTO);

        try {
//            generationPDF.generatePdfFromHtmlOld(generationPDF.parseThymeleafTemplate(paymentSchedule));
            generationPDF.generatePdfFromHtml(paymentSchedule);
        } catch (IOException | com.itextpdf.text.DocumentException e) {
            throw new RuntimeException(e);
        }




        return "OK";
    }
}
