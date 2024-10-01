package com.github.henrymorgan2.paymentschedulecalculator.service.mq;

import com.github.henrymorgan2.paymentschedulecalculator.dto.PaymentScheduleCalculatorDto;
import lombok.Data;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Data
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, PaymentScheduleCalculatorDto> kafkaTemplate;


    public void sendReport(String mail, PaymentScheduleCalculatorDto paymentScheduleCalculatorDto) {

        ProducerRecord record = new ProducerRecord<String, PaymentScheduleCalculatorDto>("pdf", paymentScheduleCalculatorDto);
        record.headers().add("user-email", mail.getBytes());
        kafkaTemplate.send(record);
    }
}
