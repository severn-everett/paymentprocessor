package com.severett.paymentprocessor.controller;

import com.severett.paymentprocessor.exceptions.TransactionExpiredException;
import com.severett.paymentprocessor.model.Transaction;
import com.severett.paymentprocessor.services.TransactionParseService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import org.json.JSONObject;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.severett.paymentprocessor.services.TransactionStorageService;
import org.json.JSONException;

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @MockBean
    private TransactionStorageService transactionStore;
    
    @MockBean
    private TransactionParseService transactionParseService;
    
    @Test
    public void getStatisticsTest() throws Exception {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("sum", 1000.0);
        statsMap.put("avg", 100.0);
        statsMap.put("max", 200.0);
        statsMap.put("min", 50.0);
        statsMap.put("count", 10L);
        given(transactionStore.getTransactionStats()).willReturn(statsMap);
        mvc.perform(get("/statistics")
                .contentType("application/json")
            ).andExpect(status().isOk())
             .andExpect(jsonPath("$.sum", is(1000)))
             .andExpect(jsonPath("$.avg", is(100)))
             .andExpect(jsonPath("$.max", is(200)))
             .andExpect(jsonPath("$.min", is(50)))
             .andExpect(jsonPath("$.count", is(10)));
    }
    
    @Test
    public void postValidTransactionTest() throws Exception {
        JSONObject postContent = new JSONObject();
        Instant now = Instant.now();
        long count = 12;
        postContent.put("count", count);
        postContent.put("timestamp", now.toEpochMilli());
        given(transactionParseService.parseTransaction(postContent.toString()))
                .willReturn(new Transaction(now, count));
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
    }
    
    @Test
    public void postExpiredTransactionTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", 3);
        postContent.put("timestamp", Instant.now().minusSeconds(100L).toEpochMilli());
        given(transactionParseService.parseTransaction(postContent.toString()))
                .willThrow(TransactionExpiredException.class);
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isNoContent());
    }
    
    @Test
    public void postBadTransactionTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", "FAIL");
        postContent.put("timestamp", Instant.now().toEpochMilli());
        given(transactionParseService.parseTransaction(postContent.toString()))
                .willThrow(new JSONException("JSONObject[\"count\"] is not a long."));
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.error", is("request")))
             .andExpect(jsonPath("$.message", is("JSONObject[\"count\"] is not a long.")));
    }
    
    @Test
    public void postInvalidTransaction() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("timestamp", Instant.now().toEpochMilli());
        given(transactionParseService.parseTransaction(postContent.toString()))
                .willThrow(new JSONException("'count' and 'timestamp' must be defined."));
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.error", is("request")))
             .andExpect(jsonPath("$.message", is("'count' and 'timestamp' must be defined.")));
    }
    
}
