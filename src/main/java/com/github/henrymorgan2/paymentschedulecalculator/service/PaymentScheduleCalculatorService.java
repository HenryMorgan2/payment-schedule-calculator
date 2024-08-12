package com.github.henrymorgan2.paymentschedulecalculator.service;

import com.github.henrymorgan2.paymentschedulecalculator.cleint.NonWorkingCalendarClient;
import com.github.henrymorgan2.paymentschedulecalculator.controllers.HttpRequestException;
import com.github.henrymorgan2.paymentschedulecalculator.dto.EntryPaymentShedule;
import com.github.henrymorgan2.paymentschedulecalculator.dto.RequestDTO;
import feign.FeignException;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
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

    private BigDecimal getMonthlyPaymentAmount(BigDecimal initialPrincipalAmount, BigDecimal interestRate, int term) {
        MathContext mathContext = new MathContext(2, RoundingMode.HALF_UP);
        BigDecimal pc = interestRate.divide(BigDecimal.valueOf(1200), mathContext);
        int pp = -term;
        return initialPrincipalAmount.multiply(pc.divide(BigDecimal.ONE.subtract((BigDecimal.ONE.add(pc)).pow(pp, mathContext)), mathContext)).setScale(2, RoundingMode.HALF_UP);

    }

    private List<EntryPaymentShedule> getCalculatePaymentSchedule(BigDecimal monthlyPaymentAmount,
                                                                  List<LocalDate> nonWorkingCalendar,
                                                                  BigDecimal initialPrincipalAmount,
                                                                  BigDecimal interestRate,
                                                                  int term,
                                                                  int paymentDay,
                                                                  LocalDate localDate) {

        MathContext mathContext = new MathContext(2, RoundingMode.HALF_UP);
        List<EntryPaymentShedule> entryPaymentShedules = new ArrayList<>();


        BigDecimal sumInterestAmountPerMonth = BigDecimal.ZERO;
        BigDecimal sumLoanBody = BigDecimal.ZERO;
        BigDecimal balanceOwed = initialPrincipalAmount;
        BigDecimal interestMonthRate = interestRate.divide(interestRate.multiply(new BigDecimal(100)), mathContext);

        for (int i = 0; i < term; i++) {
            LocalDate plusMonthsPonts = localDate.plusMonths(i + 1);

            LocalDate parsePaymentDay = LocalDate.of(plusMonthsPonts.getYear(), plusMonthsPonts.getMonthValue(), paymentDay);

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
            if (i == term - 1 | balanceOwed.compareTo(BigDecimal.ZERO) <= 0){

                balanceOwed = BigDecimal.ZERO;
            }

            entryPaymentShedules.add(new EntryPaymentShedule(workingDayOfPayment, monthlyPaymentAmount, interestAmountPerMonth, loanBody, balanceOwed));
        }
        System.out.println("Сумма процентов: " + sumInterestAmountPerMonth + " тело кредита: " + sumLoanBody);

        return entryPaymentShedules;
    }

    private LocalDate findElementInArray(List<LocalDate> nonWorkingCalendar, LocalDate parsePaymentDay) {

        LocalDate result = parsePaymentDay;

        if (nonWorkingCalendar.contains(parsePaymentDay)) {
            int parsePaymentDayUp = parsePaymentDay.plusDays(1).getDayOfMonth();
            result = LocalDate.of(parsePaymentDay.getYear(), parsePaymentDay.getMonthValue(), parsePaymentDayUp);
            return findElementInArray(nonWorkingCalendar, result);

        }
        return result;
    }


    public List<EntryPaymentShedule> getPaymentSchedule(RequestDTO requestDTO) {

        String user_email = requestDTO.getUser_email();
        BigDecimal initialPrincipalAmount = requestDTO.getInitialPrincipalAmount(); //сумма кредита
        BigDecimal interestRate = requestDTO.getInterestRate(); //годовая ставка по кредиту
        int term = requestDTO.getTerm().intValue(); //срок кредита в месяцах
        int paymentDay = requestDTO.getPaymentDay().intValue(); //дата платежа
        LocalDate localDate = LocalDate.now();

        BigDecimal monthlyPaymentAmount = getMonthlyPaymentAmount(initialPrincipalAmount, interestRate, term); //сумма платежа в месяц

        LocalDate start = localDate;
        LocalDate end = localDate.plusMonths(term + 1);

        List<LocalDate> nonWorkingCalendar = getNonWorkingDays(start, end);

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
