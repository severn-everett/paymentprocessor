package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.exceptions.TransactionExpiredException;
import com.severett.paymentprocessor.model.Transaction;
import org.json.JSONException;

public interface TransactionParseService {

    public Transaction parseTransaction(String input) throws JSONException, TransactionExpiredException;
    
}
