package com.severett.paymentprocessor.exceptions;

import java.time.Instant;

public class TransactionExpiredException extends Exception {
    
    private final Instant expiredTime;
    
    public TransactionExpiredException(Instant expiredTime) {
        super("Transaction's timestamp of " + expiredTime.toString() + " is expired");
        this.expiredTime = expiredTime;
    }
    
    public Instant getExpiredTime() {
        return expiredTime;
    }
    
}
