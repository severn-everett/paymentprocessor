package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.exceptions.InvalidUploadException;
import com.severett.paymentprocessor.exceptions.TransactionExpiredException;
import com.severett.paymentprocessor.model.Transaction;
import java.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TransactionParseServiceTest {
    
    TransactionParseService transactionParseService = new TransactionParseServiceImpl();
    
    @Test
    public void normalTransactionParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        Instant now = Instant.now();
        long count = 12;
        postContent.put("count", count);
        postContent.put("timestamp", now.toEpochMilli());
        Transaction parsedTransaction = transactionParseService.parseTransaction(postContent.toString());
        Assert.assertEquals(parsedTransaction.getCount(), count);
        Assert.assertEquals(parsedTransaction.getTimestamp(), now);
    }
    
    @Test
    public void expiredTransactionParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", 3);
        postContent.put("timestamp", Instant.now().minusSeconds(100L).toEpochMilli());
        try {
            transactionParseService.parseTransaction(postContent.toString());
            Assert.fail("Expected a TransactionExpiredException, yet none occurred.");
        } catch (TransactionExpiredException tee) {
            // No-op - expecting the transaction to be expired
        }
    }
    
    @Test
    public void badTransactionParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", "FAIL");
        postContent.put("timestamp", Instant.now().toEpochMilli());
        try {
            transactionParseService.parseTransaction(postContent.toString());
            Assert.fail("Expected a JSONException, yet none occurred.");
        } catch (JSONException jsone) {
            Assert.assertEquals("JSONObject[\"count\"] is not a long.", jsone.getMessage());
        }
    }
    
    @Test
    public void noCountTransactionParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("timestamp", Instant.now().toEpochMilli());
        try {
            transactionParseService.parseTransaction(postContent.toString());
            Assert.fail("Expected a JSONException, yet none occurred.");
        } catch (InvalidUploadException iue) {
            Assert.assertEquals("'count' and 'timestamp' must be defined.", iue.getMessage());
        }
    }
    
    @Test
    public void badCountUploadParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", "-2");
        postContent.put("timestamp", Instant.now().toEpochMilli());
        try {
            transactionParseService.parseTransaction(postContent.toString());
            Assert.fail("Expected a JSONException, yet none occurred.");
        } catch (InvalidUploadException iue) {
            Assert.assertEquals("'count' must be greater than zero.", iue.getMessage());
        }
    }
    
}
