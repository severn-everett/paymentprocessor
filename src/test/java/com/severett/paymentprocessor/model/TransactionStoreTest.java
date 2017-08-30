package com.severett.paymentprocessor.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TransactionStoreTest {
    
    TransactionStore transactionStore;
    
    @Before
    public void setup() {
        transactionStore = new TransactionStore();
    }
    
    @Test
    public void fullStatsRetrievalTest() throws InterruptedException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sum", 75.0);
        responseMap.put("avg", 25.0);
        responseMap.put("max", 50.0);
        responseMap.put("min", 5.0);
        responseMap.put("count", 3L);
        transactionStore.addTransaction(new Transaction(Instant.now(), 20.0));
        transactionStore.addTransaction(new Transaction(Instant.now(), 50.0));
        transactionStore.addTransaction(new Transaction(Instant.now(), 5.0));
        Thread.sleep(50L);
        Assert.assertEquals(responseMap, transactionStore.getTransactionStats());
    }
    
    @Test
    public void partlyExpiredStatsRetrievalTest() throws InterruptedException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sum", 40.0);
        responseMap.put("avg", 20.0);
        responseMap.put("max", 30.0);
        responseMap.put("min", 10.0);
        responseMap.put("count", 2L);
        transactionStore.addTransaction(new Transaction(Instant.now().minusSeconds(58L), 100.0));
        transactionStore.addTransaction(new Transaction(Instant.now(), 30.0));
        transactionStore.addTransaction(new Transaction(Instant.now(), 10.0));
        System.out.println("Sleeping 5 seconds to expire one transaction...");
        Thread.sleep(5000L);
        Assert.assertEquals(responseMap, transactionStore.getTransactionStats());
    }
    
}
