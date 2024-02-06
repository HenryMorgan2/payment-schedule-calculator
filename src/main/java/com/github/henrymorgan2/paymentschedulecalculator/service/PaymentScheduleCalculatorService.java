package com.github.henrymorgan2.paymentschedulecalculator.service;

import com.github.henrymorgan2.paymentschedulecalculator.cleint.NonWorkingCalendarClient;
import com.github.henrymorgan2.paymentschedulecalculator.controllers.HttpRequestException;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import feign.FeignException;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
public class PaymentScheduleCalculatorService {

    private final NonWorkingCalendarClient nonWorkingCalendarClient;

    List<LocalDate> getNonWorkingDays(LocalDate start, LocalDate end) {

        List<LocalDate> response;

        try {
            response = nonWorkingCalendarClient.getNonWorkingDays(start, end).getNonWorkingDays();
        }catch (FeignException exception){
            String responseBody = null;
            if (exception.responseBody().isPresent()){
                responseBody = new String(exception.responseBody().get().array());
            }
            throw new HttpRequestException(responseBody, exception.status());


        }

        return response;
    }

    private double getMonthlyPaymentAmount(BigDecimal initialPrincipalAmount, BigDecimal interestRate, BigDecimal term) {

        double pc = interestRate.doubleValue() / (100 * 12);
        double o = initialPrincipalAmount.doubleValue();
        double pp = -term.doubleValue();

        //Преобразуем в BigDecimal, округляем в большую сторону и возвращаем double
        return  new BigDecimal(o * (pc / (1 - Math.pow((1 + pc), pp)))).setScale(2, RoundingMode.HALF_UP).doubleValue();
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

        double sumInterestAmountPerMonth = 0;
        double sumLoanBody = 0;
        double balanceOwed = initialPrincipalAmount.doubleValue();
        double interestMonthRate = interestRate.doubleValue() / (100*interestRate.doubleValue());
        while (i < intTerm) {
            LocalDate plusMonthsPonts = localDate.plusMonths(i + 1);

            String plusMonthsPontsVerify = String.valueOf(plusMonthsPonts.getMonthValue());
            if (plusMonthsPonts.getMonthValue() < 10) {
                plusMonthsPontsVerify = "0" + plusMonthsPontsVerify;
            }

            LocalDate parsePaymentDay = LocalDate.parse(plusMonthsPonts.getYear() + "-" + plusMonthsPontsVerify + "-" + paymentDayVerify);

            //Дата платежа
            LocalDate workingDayOfPayment = findElementInArray(nonWorkingCalendar, parsePaymentDay);

            //Сумма процентов в месяце
            double interestAmountPerMonth = new BigDecimal(balanceOwed * interestMonthRate).setScale(2, RoundingMode.HALF_UP).doubleValue();

            sumInterestAmountPerMonth = sumInterestAmountPerMonth + interestAmountPerMonth;
            DecimalFormat decimalFormat = new DecimalFormat("#.##"); // округляем до 2-х знаков после запятой

            //Тело кредита
            double loanBody = BigDecimal.valueOf(monthlyPaymentAmount).subtract(BigDecimal.valueOf(interestAmountPerMonth)).doubleValue();
            sumLoanBody = sumLoanBody + loanBody;

            //Остаток основного дога
            balanceOwed = BigDecimal.valueOf(balanceOwed).subtract(BigDecimal.valueOf(monthlyPaymentAmount).subtract(BigDecimal.valueOf(interestAmountPerMonth))).doubleValue();

            //Убираем потерю точности
            if (balanceOwed < 0){
                balanceOwed = 0;
            }

            System.out.println("Дата платежа: " + workingDayOfPayment.toString() + " платеж: " + monthlyPaymentAmount + " проценты: " + interestAmountPerMonth + " тело кредита: " + loanBody + " сумма основного долга: " + balanceOwed);

            i++;
        }
        System.out.println("Сумма процентов: " + sumInterestAmountPerMonth + " тело кредита: " + sumLoanBody);

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

        LocalDate start = localDate;
        LocalDate end = localDate.plusMonths(term.longValue() + 1);

        List<LocalDate> nonWorkingCalendar = getNonWorkingDays(start, end);

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
