package com.github.henrymorgan2.paymentschedulecalculator.cleint;

import com.github.henrymorgan2.paymentschedulecalculator.controllers.HttpRequestException;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

public class RetryErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = feign.FeignException.errorStatus(methodKey, response);
        int status = response.status();
        if (status >= 500) {

            byte[] responseBody = {};
            if (exception.responseBody().isPresent()){
                responseBody = exception.responseBody().get().array();
            }

            return new RetryableException(
                    response.status(),
                    exception.getMessage(),
                    response.request().httpMethod(),
                    null,
                    response.request(),
                    responseBody,
                    null
            );
        }
        return exception;
    }
}
