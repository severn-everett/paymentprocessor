package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.model.Transaction;
import java.util.Map;

public interface TransactionStore {
    
    public void addTransaction(Transaction transaction);
    
    public Map<String, Object> getTransactionStats();
    
}
