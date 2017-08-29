package com.severett.paymentprocessor.model;

import java.time.Instant;

public final class Transaction {
    
    private final Instant timestamp;
    private final double amt;
    
    public Transaction(Instant timestamp, double amt) {
        this.timestamp = timestamp;
        this.amt = amt;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public double getAmt() {
        return amt;
    }
    
}
