package com.severett.paymentprocessor.model;

import java.time.Instant;

public final class Upload {
    
    private final Instant timestamp;
    private final long count;
    
    public Upload(Instant timestamp, long count) {
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
