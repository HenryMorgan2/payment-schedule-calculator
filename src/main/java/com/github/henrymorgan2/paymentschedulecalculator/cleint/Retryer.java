package com.github.henrymorgan2.paymentschedulecalculator.cleint;

import feign.RetryableException;

public class Retryer implements feign.Retryer {
    private final int maxAttempts;
    private final long backoff;
    int attempt;

    public Retryer() {
        this(1000, 3);
    }

    public Retryer(long backoff, int maxAttempts) {
        this.backoff = backoff;
        this.maxAttempts = maxAttempts;
        this.attempt = 1;
    }

    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            throw e;
        }

        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Retryer clone() {
        return new Retryer(backoff, maxAttempts);
    }
}
