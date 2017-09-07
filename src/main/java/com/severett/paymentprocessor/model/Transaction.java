package com.severett.paymentprocessor.model;

import java.time.Instant;

public final class Transaction {
    
    private final Instant timestamp;
    private final long count;
    
    public Transaction(Instant timestamp, long count) {
        this.timestamp = timestamp;
        this.count = count;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public long getCount() {
        return count;
    }
    
}
