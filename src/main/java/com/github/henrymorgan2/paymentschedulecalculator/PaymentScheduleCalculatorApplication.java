package com.github.henrymorgan2.paymentschedulecalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentScheduleCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentScheduleCalculatorApplication.class, args);
	}

}
