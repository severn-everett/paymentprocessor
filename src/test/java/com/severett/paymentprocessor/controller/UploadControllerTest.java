package com.severett.paymentprocessor.controller;

import com.severett.paymentprocessor.exceptions.UploadExpiredException;
import com.severett.paymentprocessor.model.Upload;
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
import java.util.Optional;
import org.json.JSONException;
import com.severett.paymentprocessor.services.UploadParseService;
import com.severett.paymentprocessor.services.UploadStorageService;

@RunWith(SpringRunner.class)
@WebMvcTest(UploadController.class)
public class UploadControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @MockBean
    private UploadStorageService uploadStore;
    
    @MockBean
    private UploadParseService uploadParseService;
    
    @Test
    public void getStatisticsTest() throws Exception {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("sum", 1000.0);
        statsMap.put("avg", 100.0);
        statsMap.put("max", 200.0);
        statsMap.put("min", 50.0);
        statsMap.put("count", 10L);
        given(uploadStore.getUploadStats()).willReturn(Optional.of(statsMap));
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
    public void postValidUploadTest() throws Exception {
        JSONObject postContent = new JSONObject();
        Instant now = Instant.now();
        long count = 12;
        postContent.put("count", count);
        postContent.put("timestamp", now.toEpochMilli());
        given(uploadParseService.parseUpload(postContent.toString()))
                .willReturn(new Upload(now, count));
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isCreated());
        
    }
    
    @Test
    public void postExpiredUploadTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", 3);
        postContent.put("timestamp", Instant.now().minusSeconds(100L).toEpochMilli());
        given(uploadParseService.parseUpload(postContent.toString()))
                .willThrow(UploadExpiredException.class);
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isNoContent());
    }
    
    @Test
    public void postBadUploadTest() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("count", "FAIL");
        postContent.put("timestamp", Instant.now().toEpochMilli());
        given(uploadParseService.parseUpload(postContent.toString()))
                .willThrow(new JSONException("JSONObject[\"count\"] is not a long."));
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.error", is("request")))
             .andExpect(jsonPath("$.message", is("JSONObject[\"count\"] is not a long.")));
    }
    
    @Test
    public void postInvalidUpload() throws Exception {
        JSONObject postContent = new JSONObject();
        postContent.put("timestamp", Instant.now().toEpochMilli());
        given(uploadParseService.parseUpload(postContent.toString()))
                .willThrow(new JSONException("'count' and 'timestamp' must be defined."));
        mvc.perform(post("/upload")
                .contentType("application/json")
                .content(postContent.toString())
            ).andExpect(status().isBadRequest())
             .andExpect(jsonPath("$.error", is("request")))
             .andExpect(jsonPath("$.message", is("'count' and 'timestamp' must be defined.")));
    }
    
}
