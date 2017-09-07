package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.model.Transaction;
import java.util.Map;

public interface TransactionStorageService {
    
    public void addTransaction(Transaction transaction);
    
    public Map<String, Object> getTransactionStats();
    
}
