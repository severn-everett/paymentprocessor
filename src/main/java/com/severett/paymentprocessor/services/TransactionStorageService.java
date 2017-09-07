package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.model.Transaction;
import java.util.Map;
import java.util.Optional;

public interface TransactionStorageService {
    
    public void addTransaction(Transaction transaction);
    
    public Optional<Map<String, Object>> getTransactionStats();
    
}
