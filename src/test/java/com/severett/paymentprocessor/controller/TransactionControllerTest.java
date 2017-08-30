package com.severett.paymentprocessor.controller;

import com.severett.paymentprocessor.model.ITransactionStore;
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
    
}
