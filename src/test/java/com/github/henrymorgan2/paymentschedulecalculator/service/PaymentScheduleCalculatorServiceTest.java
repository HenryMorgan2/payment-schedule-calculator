package com.github.henrymorgan2.paymentschedulecalculator.service;

import com.github.henrymorgan2.paymentschedulecalculator.controllers.HttpRequestException;
import com.github.henrymorgan2.paymentschedulecalculator.utils.FileUtils;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
public class PaymentScheduleCalculatorServiceTest{

    static LocalDate start = LocalDate.of(2023,11,01);
    static LocalDate end = LocalDate.of(2023,11,07);
    static String testUrl = "/api/non-working-days?start=2023-11-01&end=2023-11-07";
    static String testUrlEnd = "/api/non-working-days?start=2023-11-01";
    static String testUrlStart = "/api/non-working-days?end=2023-11-07";

    @Autowired
    private PaymentScheduleCalculatorService paymentScheduleCalculatorService;

    @AfterEach
    public void reset(){
        WireMock.reset();
    }

    @Test
    @DisplayName("Сервис получения выходных дней. Код статуса: 200")
    public void getNonWorkingCalendar_whenAllIsOk_thenReturnOkStatus() throws Exception{

        List<LocalDate> expected = Arrays.asList(
                LocalDate.of(2023,11,04)
                , LocalDate.of(2023,11,05));

        // given
        stubFor(WireMock.get(testUrl)
                .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody(FileUtils.getFileAsString("service/getNonWorkingDays_200.json"))));

        // when
        List<LocalDate> response = paymentScheduleCalculatorService.getNonWorkingDays(start, end);

        // then
        assertThat(response).isNotNull();
        assertEquals(expected, response);
        verify(exactly(1), getRequestedFor(urlEqualTo(testUrl)));

    }
    @Test
    @DisplayName("Сервис получения выходных дней. Код статуса: 400, отсутстсвует параметр end")
    public void getNonWorkingDays_whenRequestParamsDoesNotMatchType_thenReturnBadRequestStatus_end() throws Exception{

        String errorResponse = FileUtils.getFileAsString("service/getNonWorkingDays_400_end.json");

        // given
        stubFor(WireMock.get(testUrlEnd)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                        .withBody(errorResponse)));

        // when
        HttpRequestException actual = assertThrows(HttpRequestException.class, ()-> paymentScheduleCalculatorService.getNonWorkingDays(start, null));
        // then

        assertThat(actual.getHttpStatus()).isEqualTo(400);
        assertThat(actual.getResponseBody()).isEqualTo(errorResponse);
        verify(exactly(1), getRequestedFor(urlEqualTo(testUrlEnd)));

    }

    @Test
    @DisplayName("Сервис получения выходных дней. Код статуса: 400, отсутстсвует параметр start")
    public void getNonWorkingDays_whenRequestParamsDoesNotMatchType_thenReturnBadRequestStatus_start() throws Exception{

        String errorResponse = FileUtils.getFileAsString("service/getNonWorkingDays_400_start.json");

        // given
        stubFor(WireMock.get(testUrlStart)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                        .withBody(errorResponse)));

        // when
        HttpRequestException actual = assertThrows(HttpRequestException.class, ()-> paymentScheduleCalculatorService.getNonWorkingDays(null, end));
        // then

        assertThat(actual.getHttpStatus()).isEqualTo(400);
        assertThat(actual.getResponseBody()).isEqualTo(errorResponse);
        verify(exactly(1), getRequestedFor(urlEqualTo(testUrlStart)));

    }

    @Test
    @DisplayName("Сервис получения выходных дней. Код статуса: 500")
    public void getNonWorkingDays_ConnectException() throws Exception{

        String errorResponse = FileUtils.getFileAsString("service/getNonWorkingDays_500.json");

        // given
        stubFor(WireMock.get(testUrl)
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(500)
                        .withBody(errorResponse)));
        // when
        HttpRequestException actual = assertThrows(HttpRequestException.class, ()-> paymentScheduleCalculatorService.getNonWorkingDays(start, end));
        // then
        assertThat(actual.getHttpStatus()).isEqualTo(500);
        assertThat(actual.getResponseBody()).isEqualTo(errorResponse);
        verify(exactly(3), getRequestedFor(urlEqualTo(testUrl)));
    }
}
