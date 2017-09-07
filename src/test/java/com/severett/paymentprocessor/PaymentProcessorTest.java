package com.severett.paymentprocessor;

import java.time.Instant;
import static org.hamcrest.Matchers.is;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
  webEnvironment = WebEnvironment.RANDOM_PORT,
  classes = PaymentProcessor.class)
@AutoConfigureMockMvc
public class PaymentProcessorTest {
    
    @Autowired
    private MockMvc mvc;
    
    @Test
    public void transactionsAndStatisticsTest() throws Exception {
        JSONObject postContent = new JSONObject();
        
        // Post first entry
        postContent.put("count", 10);
        postContent.put("timestamp", Instant.now().toEpochMilli());
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
        // Post second entry
        postContent.put("count", 50);
        postContent.put("timestamp", Instant.now().toEpochMilli());
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
        // Post expired entry
        postContent.put("count", 1000);
        postContent.put("timestamp", Instant.now().minusSeconds(65L).toEpochMilli());
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isNoContent());
        
        // Post soon-expiring entry
        postContent.put("count", 20);
        postContent.put("timestamp", Instant.now().minusSeconds(58L).toEpochMilli());
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
        // Post third entry
        postContent.put("count", 30);
        postContent.put("timestamp", Instant.now().toEpochMilli());
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
        System.out.println("Sleeping 5 seconds to expire one transaction...");
        Thread.sleep(5000L);
        
        mvc.perform(get("/statistics")
                .contentType("application/json")
            ).andExpect(status().isOk())
             .andExpect(jsonPath("$.sum", is(90)))
             .andExpect(jsonPath("$.avg", is(30)))
             .andExpect(jsonPath("$.max", is(50)))
             .andExpect(jsonPath("$.min", is(10)))
             .andExpect(jsonPath("$.count", is(3)));
    }
    
}
