package com.severett.paymentprocessor.model;

import com.severett.paymentprocessor.services.TransactionStorageServiceImpl;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TransactionStoreTest {
    
    TransactionStorageServiceImpl transactionStore;
    
    @Before
    public void setup() {
        transactionStore = new TransactionStorageServiceImpl();
    }
    
    @Test
    public void fullStatsRetrievalTest() throws InterruptedException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sum", 75L);
        responseMap.put("avg", 25.0);
        responseMap.put("max", 50L);
        responseMap.put("min", 5L);
        responseMap.put("count", 3L);
        transactionStore.addTransaction(new Transaction(Instant.now(), 20));
        transactionStore.addTransaction(new Transaction(Instant.now(), 50));
        transactionStore.addTransaction(new Transaction(Instant.now(), 5));
        Assert.assertEquals(responseMap, transactionStore.getTransactionStats().get());
    }
    
    @Test
    public void partlyExpiredStatsRetrievalTest() throws InterruptedException {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sum", 40L);
        responseMap.put("avg", 20.0);
        responseMap.put("max", 30L);
        responseMap.put("min", 10L);
        responseMap.put("count", 2L);
        transactionStore.addTransaction(new Transaction(Instant.now().minusSeconds(58L), 100));
        transactionStore.addTransaction(new Transaction(Instant.now(), 30));
        transactionStore.addTransaction(new Transaction(Instant.now(), 10));
        System.out.println("Sleeping 5 seconds to expire one transaction...");
        Thread.sleep(5000L);
        Assert.assertEquals(responseMap, transactionStore.getTransactionStats().get());
    }
    
}
