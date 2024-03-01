package com.github.henrymorgan2.paymentschedulecalculator.service;

import com.github.henrymorgan2.paymentschedulecalculator.cleint.NonWorkingCalendarClient;
import com.github.henrymorgan2.paymentschedulecalculator.controllers.HttpRequestException;
import com.github.henrymorgan2.paymentschedulecalculator.dto.EntryPaymentShedule;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import feign.FeignException;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private BigDecimal getMonthlyPaymentAmount(BigDecimal initialPrincipalAmount, BigDecimal interestRate, BigDecimal term) {

        double pc = interestRate.doubleValue() / (100 * 12);
        double o = initialPrincipalAmount.doubleValue();
        double pp = -term.doubleValue();

        //Преобразуем в BigDecimal, округляем в большую сторону и возвращаем double
        return  new BigDecimal(o * (pc / (1 - Math.pow((1 + pc), pp)))).setScale(2, RoundingMode.HALF_UP);
    }

    private List<EntryPaymentShedule> getCalculatePaymentSchedule(BigDecimal monthlyPaymentAmount,
                                                      List<LocalDate> nonWorkingCalendar,
                                                      BigDecimal initialPrincipalAmount,
                                                      BigDecimal interestRate,
                                                      BigDecimal term,
                                                      BigDecimal paymentDay,
                                                      LocalDate localDate) {


        List<EntryPaymentShedule> entryPaymentShedules = new ArrayList<>();

        String paymentDayVerify = paymentDay.toString();
        if (paymentDay.intValue() < 10) {
            paymentDayVerify = "0" + paymentDayVerify;
        }

        int i = 0;
        int intTerm = term.intValue();

        BigDecimal sumInterestAmountPerMonth = new BigDecimal(0);
        BigDecimal sumLoanBody = new BigDecimal(0);
        BigDecimal balanceOwed = initialPrincipalAmount;
        BigDecimal interestMonthRate = interestRate.divide(interestRate.multiply(new BigDecimal(100)));
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
            BigDecimal interestAmountPerMonth = balanceOwed.multiply(interestMonthRate).setScale(2, RoundingMode.HALF_UP);

            //Тело кредита
            BigDecimal loanBody = monthlyPaymentAmount.subtract(interestAmountPerMonth);
            sumLoanBody = sumLoanBody.add(loanBody);

            //Остаток основного дога
            balanceOwed = balanceOwed.subtract(monthlyPaymentAmount.subtract(interestAmountPerMonth));

            //Убираем потерю точности
            if (i == intTerm - 1 | balanceOwed.compareTo(new BigDecimal(0)) <= 0){

                balanceOwed = new BigDecimal(0);
            }

            entryPaymentShedules.add(new EntryPaymentShedule(workingDayOfPayment.toString(),monthlyPaymentAmount,interestAmountPerMonth,loanBody,balanceOwed));

            i++;
        }
        System.out.println("Сумма процентов: " + sumInterestAmountPerMonth + " тело кредита: " + sumLoanBody);

        return entryPaymentShedules;
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


    public List<EntryPaymentShedule> getPaymentSchedule(RequestDTO requestDTO) {

        String user_email = requestDTO.getUser_email();
        BigDecimal initialPrincipalAmount = requestDTO.getInitialPrincipalAmount(); //сумма кредита
        BigDecimal interestRate = requestDTO.getInterestRate(); //годовая ставка по кредиту
        BigDecimal term = requestDTO.getTerm(); //срок кредита в месяцах
        BigDecimal paymentDay = requestDTO.getPaymentDay(); //дата платежа
        LocalDate localDate = LocalDate.now();

        BigDecimal monthlyPaymentAmount = getMonthlyPaymentAmount(initialPrincipalAmount, interestRate, term); //сумма платежа в месяц

        LocalDate start = localDate;
        LocalDate end = localDate.plusMonths(term.longValue() + 1);

        List<LocalDate> nonWorkingCalendar = getNonWorkingDays(start, end);

//        for (LocalDate entry: nonWorkingCalendar) {
//            System.out.println(entry);
//        }


        List<EntryPaymentShedule> calculatePaymentSchedule = getCalculatePaymentSchedule(monthlyPaymentAmount,
                nonWorkingCalendar,
                initialPrincipalAmount,
                interestRate,
                term,
                paymentDay,
                localDate);

        return calculatePaymentSchedule;
    }


}
