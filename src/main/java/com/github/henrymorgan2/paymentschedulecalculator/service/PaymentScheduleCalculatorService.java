package com.github.henrymorgan2.paymentschedulecalculator.service;

import com.github.henrymorgan2.paymentschedulecalculator.cleint.PaymentScheduleCalculatorExternalApi;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class PaymentScheduleCalculatorService {

    private final PaymentScheduleCalculatorExternalApi paymentScheduleCalculatorExternalApi;

    private List<LocalDate> getNonWorkingCalendar(BigDecimal term, LocalDate localDate) {


        String start = localDate.toString();
        String end = localDate.plusMonths(term.longValue() + 1).toString();

        return paymentScheduleCalculatorExternalApi.getNonWorkingCalendar(start, end).getNonWorkingDays();
    }

    private double getMonthlyPaymentAmount(BigDecimal initialPrincipalAmount, BigDecimal interestRate, BigDecimal term) {

        double pc = interestRate.doubleValue() / (100 * 12);
        double o = initialPrincipalAmount.doubleValue();
        double pp = -term.doubleValue();
        return o * (pc / (1 - Math.pow((1 + pc), pp)));

    }

    private List<HashMap> getCalculatePaymentSchedule(double monthlyPaymentAmount,
                                                      List<LocalDate> nonWorkingCalendar,
                                                      BigDecimal initialPrincipalAmount,
                                                      BigDecimal interestRate,
                                                      BigDecimal term,
                                                      BigDecimal paymentDay,
                                                      LocalDate localDate) {


        String paymentDayVerify = paymentDay.toString();
        if (paymentDay.intValue() < 10) {
            paymentDayVerify = "0" + paymentDayVerify;
        }

        int i = 0;
        int intTerm = term.intValue();

        double balanceOwed = initialPrincipalAmount.doubleValue();
        double interestRatePerYear = interestRate.doubleValue() / 100;
        while (i <= intTerm) {
            LocalDate plusMonthsPonts = localDate.plusMonths(i + 1);

            String plusMonthsPontsVerify = String.valueOf(plusMonthsPonts.getMonthValue());
            if (plusMonthsPonts.getMonthValue() < 10) {
                plusMonthsPontsVerify = "0" + plusMonthsPontsVerify;
            }

            LocalDate parsePaymentDay = LocalDate.parse(plusMonthsPonts.getYear() + "-" + plusMonthsPontsVerify + "-" + paymentDayVerify);

            //Дата платежа
            LocalDate workingDayOfPayment = findElementInArray(nonWorkingCalendar, parsePaymentDay);

            //Сумма процентов в месяце
            double InterestAmountPerMonth = (balanceOwed * interestRatePerYear * 30) / 365;

            //Остаток основного дога
            balanceOwed = balanceOwed - (monthlyPaymentAmount - InterestAmountPerMonth);

            System.out.println("Дата платежа: " + workingDayOfPayment.toString() + " сумма процентов: " + monthlyPaymentAmount + " сумма основного долга: " + balanceOwed);

            i++;
        }


        return new ArrayList<>();
    }

    private LocalDate findElementInArray(List<LocalDate> nonWorkingCalendar, LocalDate parsePaymentDay) {

        LocalDate result = parsePaymentDay;

        if (nonWorkingCalendar.stream().filter(localDateNonWorkingCalendar -> localDateNonWorkingCalendar.equals(parsePaymentDay)).collect(Collectors.toList()).size() > 0) {

            int parsePaymentDayUp = parsePaymentDay.plusDays(1).getDayOfMonth();

            String paymentDayVerify = String.valueOf(parsePaymentDayUp);
            if (parsePaymentDayUp < 10) {
                paymentDayVerify = "0" + paymentDayVerify;
            }
            String paymentMonthVerify = String.valueOf(parsePaymentDay.getMonthValue());
            if (parsePaymentDay.getMonthValue() < 10) {
                paymentMonthVerify = "0" + paymentMonthVerify;
            }

            result = LocalDate.parse(parsePaymentDay.getYear() + "-" + paymentMonthVerify + "-" + paymentDayVerify);
            return findElementInArray(nonWorkingCalendar, result);

        }
        return result;
    }


    public List<HashMap> getPaymentSchedule(RequestDTO requestDTO) {

        String user_email = requestDTO.getUser_email();
        BigDecimal initialPrincipalAmount = requestDTO.getInitialPrincipalAmount(); //сумма кредита
        BigDecimal interestRate = requestDTO.getInterestRate(); //годовая ставка по кредиту
        BigDecimal term = requestDTO.getTerm(); //срок кредита в месяцах
        BigDecimal paymentDay = requestDTO.getPaymentDay(); //дата платежа
        LocalDate localDate = LocalDate.now();

        double monthlyPaymentAmount = getMonthlyPaymentAmount(initialPrincipalAmount, interestRate, term); //сумма платежа в месяц

        List<LocalDate> nonWorkingCalendar = getNonWorkingCalendar(term, localDate);

        List<HashMap> calculatePaymentSchedule = getCalculatePaymentSchedule(monthlyPaymentAmount,
                nonWorkingCalendar,
                initialPrincipalAmount,
                interestRate,
                term,
                paymentDay,
                localDate);

        return new ArrayList<>();
    }


}
