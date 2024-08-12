package com.github.henrymorgan2.paymentschedulecalculator.service.mq;

import lombok.Data;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Data
public class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendReport(String mail, String paymentScheduleCalculator){

        ProducerRecord record = new ProducerRecord<String, String>("pdf2", paymentScheduleCalculator);
        record.headers().add("user-email", mail.getBytes());
        System.out.println("перед send");
        kafkaTemplate.send(record);
        System.out.println("после send");

    }
}
