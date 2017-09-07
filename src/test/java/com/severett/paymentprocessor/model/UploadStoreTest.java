package com.severett.paymentprocessor.model;

import com.severett.paymentprocessor.services.UploadStorageServiceImpl;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class UploadStoreTest {
    
    UploadStorageServiceImpl uploadStore;
    
    @Before
    public void setup() {
        uploadStore = new UploadStorageServiceImpl();
    }
    
    @Test
    public void fullStatsRetrievalTest() throws InterruptedException {
        uploadStore.addUpload(new Upload(Instant.now(), 20));
        uploadStore.addUpload(new Upload(Instant.now(), 50));
        uploadStore.addUpload(new Upload(Instant.now(), 5));
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sum", 75L);
        responseMap.put("avg", 25.0);
        responseMap.put("max", 50L);
        responseMap.put("min", 5L);
        responseMap.put("count", 3L);
        Assert.assertEquals(responseMap, uploadStore.getUploadStats().get());
    }
    
    @Test
    public void partlyExpiredStatsRetrievalTest() throws InterruptedException {
        uploadStore.addUpload(new Upload(Instant.now().minusSeconds(58L), 100));
        uploadStore.addUpload(new Upload(Instant.now(), 30));
        uploadStore.addUpload(new Upload(Instant.now(), 10));
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sum", 40L);
        responseMap.put("avg", 20.0);
        responseMap.put("max", 30L);
        responseMap.put("min", 10L);
        responseMap.put("count", 2L);
        System.out.println("Sleeping 5 seconds to expire one upload...");
        Thread.sleep(5000L);
        Assert.assertEquals(responseMap, uploadStore.getUploadStats().get());
    }
    
}
