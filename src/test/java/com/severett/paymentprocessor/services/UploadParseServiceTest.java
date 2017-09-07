package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.exceptions.InvalidUploadException;
import com.severett.paymentprocessor.exceptions.UploadExpiredException;
import com.severett.paymentprocessor.model.Upload;
import java.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class UploadParseServiceTest {
    
    UploadParseService uploadParseService = new UploadParseServiceImpl();
    
    @Test
    public void normalUploadParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        Instant now = Instant.now();
        long count = 12;
        postContent.put("count", count);
        postContent.put("timestamp", now.toEpochMilli());
        Upload parsedUpload = uploadParseService.parseUpload(postContent.toString());
        Assert.assertEquals(parsedUpload.getCount(), count);
        Assert.assertEquals(parsedUpload.getTimestamp(), now);
    }
    
    @Test
    public void expiredUploadParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", 3);
        postContent.put("timestamp", Instant.now().minusSeconds(100L).toEpochMilli());
        try {
            uploadParseService.parseUpload(postContent.toString());
            Assert.fail("Expected a TransactionExpiredException, yet none occurred.");
        } catch (UploadExpiredException tee) {
            // No-op - expecting the transaction to be expired
        }
    }
    
    @Test
    public void badUploadParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", "FAIL");
        postContent.put("timestamp", Instant.now().toEpochMilli());
        try {
            uploadParseService.parseUpload(postContent.toString());
            Assert.fail("Expected a JSONException, yet none occurred.");
        } catch (JSONException jsone) {
            Assert.assertEquals("JSONObject[\"count\"] is not a long.", jsone.getMessage());
        }
    }
    
    @Test
    public void noCountUploadParseTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("timestamp", Instant.now().toEpochMilli());
        try {
            uploadParseService.parseUpload(postContent.toString());
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
            uploadParseService.parseUpload(postContent.toString());
            Assert.fail("Expected a JSONException, yet none occurred.");
        } catch (InvalidUploadException iue) {
            Assert.assertEquals("'count' must be greater than zero.", iue.getMessage());
        }
    }
    
}
