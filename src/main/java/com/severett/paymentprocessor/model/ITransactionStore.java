package com.severett.paymentprocessor.model;

import java.util.Map;

public interface ITransactionStore {
    
    public void addTransaction(Transaction transaction);
    
    public Map<String, Object> getTransactionStats();
    
}
