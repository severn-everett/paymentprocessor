package com.severett.paymentprocessor.controller;

import com.severett.paymentprocessor.model.ITransactionStore;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import org.json.JSONObject;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @MockBean
    private ITransactionStore transactionStore;
    
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
        postContent.put("amount", 12.3);
        postContent.put("timestamp", Instant.now().toEpochMilli());
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
    }
    
    @Test
    public void postExpiredTransactionTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("amount", 3.50);
        postContent.put("timestamp", Instant.now().minusSeconds(100L).toEpochMilli());
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isNoContent());
    }
    
    @Test
    public void postBadTransactionTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("amount", "FAIL");
        postContent.put("timestamp", Instant.now().toEpochMilli());
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.error", is("request")))
             .andExpect(jsonPath("$.message", is("JSONObject[\"amount\"] is not a number.")));
    }
    
    @Test
    public void postInvalidTransaction() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("timestamp", Instant.now().toEpochMilli());
        mvc.perform(post("/transactions")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.error", is("request")))
             .andExpect(jsonPath("$.message", is("'amt' and 'timestamp' must be defined.")));
    }
    
}
