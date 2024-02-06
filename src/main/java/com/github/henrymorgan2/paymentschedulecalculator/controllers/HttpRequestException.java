package com.github.henrymorgan2.paymentschedulecalculator.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class HttpRequestException extends RuntimeException {

    private String responseBody;
    private int httpStatus;

}
