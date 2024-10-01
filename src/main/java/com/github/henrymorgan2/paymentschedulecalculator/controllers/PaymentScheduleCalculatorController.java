package com.github.henrymorgan2.paymentschedulecalculator.controllers;

import com.github.henrymorgan2.paymentschedulecalculator.dto.EntryPaymentShedule;
import com.github.henrymorgan2.paymentschedulecalculator.dto.PaymentScheduleCalculatorDto;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import com.github.henrymorgan2.paymentschedulecalculator.service.PaymentScheduleCalculatorService;
import com.github.henrymorgan2.paymentschedulecalculator.service.mq.KafkaProducer;
import com.github.henrymorgan2.paymentschedulecalculator.utils.GenerationPDF;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentScheduleCalculatorController {

    private final PaymentScheduleCalculatorService paymentScheduleCalculatorService;
    private final GenerationPDF generationPDF;
    private final KafkaProducer kafkaProducer;

    @PostMapping("/calculate-payment-schedule")
    public String getAPaymentSchedule(@RequestBody() RequestDTO requestDTO) throws IOException {

        List<EntryPaymentShedule> paymentSchedule = paymentScheduleCalculatorService.getPaymentSchedule(requestDTO);

        try {

            String encoded = Base64.getEncoder().encodeToString(generationPDF.generatePdfFromList(paymentSchedule));

            PaymentScheduleCalculatorDto paymentScheduleCalculatorDto = new PaymentScheduleCalculatorDto(encoded);

            kafkaProducer.sendReport(requestDTO.getUserEmail(), paymentScheduleCalculatorDto);


        } catch (com.itextpdf.text.DocumentException e) {
            throw new RuntimeException(e);
        }

        return "OK";
    }
}
