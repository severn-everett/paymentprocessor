package com.severett.paymentprocessor.exceptions;

import java.time.Instant;

public class UploadExpiredException extends Exception {
    
    private final Instant expiredTime;
    
    public UploadExpiredException(Instant expiredTime) {
        super("Transaction's timestamp of " + expiredTime.toString() + " is expired");
        this.expiredTime = expiredTime;
    }
    
    public Instant getExpiredTime() {
        return expiredTime;
    }
    
}
